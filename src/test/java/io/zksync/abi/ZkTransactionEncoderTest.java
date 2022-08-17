package io.zksync.abi;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import io.zksync.helper.CounterContract;
import io.zksync.methods.request.Eip712Meta;
import io.zksync.transaction.type.Transaction712;
import io.zksync.utils.ContractDeployer;
import io.zksync.utils.ZkSyncAddresses;
import io.zksync.wrappers.L2ETHBridge;
import org.junit.Before;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import io.zksync.protocol.core.Token;
import org.web3j.utils.Numeric;

public class ZkTransactionEncoderTest {

    private static final Token ETH = Token.createETH();
    private static final String BRIDGE_ADDRESS = "0x8c98381FfE6229Ee9E53B6aAb784E86863f61885";
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(42);
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(43);
    private static final Long CHAIN_ID = 270L;

    Credentials credentials;

    @Before
    public void setUp() {
        this.credentials = Credentials.create(ECKeyPair.create(BigInteger.ONE));
    }

    @Test
    public void testEncodeWithdraw() {
        final Function withdraw = new Function(
                L2ETHBridge.FUNC_WITHDRAW,
                Arrays.asList(new Address(credentials.getAddress()),
                        new Address(ETH.getL2Address()),
                        new Uint256(ETH.toBigInteger(1))),
                Collections.emptyList());

        String calldata = FunctionEncoder.encode(withdraw);

        Transaction712 transaction = new Transaction712(
                BigInteger.ZERO,
                GAS_PRICE,
                GAS_LIMIT,
                BRIDGE_ADDRESS,
                BigInteger.ZERO,
                calldata,
                CHAIN_ID,
                new Eip712Meta(
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        null,
                        null,
                        null
                )
        );

        String expected = "0x71f89a802b2a948c98381ffe6229ee9e53b6aab784e86863f6188580b864d9caed120000000000000000000000007e5f4552091a69125d5dfcb7b8c2659029395bdf00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000de0b6b3a764000082010e94000000000000000000000000000000000000000080c0c0";

        assertEquals(expected, Numeric.toHexString(TransactionEncoder.encode(transaction, null)));
    }

    @Test
    public void testEncodeDeploy() {
        byte[] bytecodeBytes = Numeric.hexStringToByteArray(CounterContract.BINARY);
        String calldata = FunctionEncoder.encode(ContractDeployer.encodeCreate2(bytecodeBytes));

        Transaction712 transaction = new Transaction712(
                BigInteger.ZERO,
                GAS_PRICE,
                GAS_LIMIT,
                ZkSyncAddresses.CONTRACT_DEPLOYER_ADDRESS,
                BigInteger.ZERO,
                calldata,
                CHAIN_ID,
                new Eip712Meta(
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        new byte[][]{bytecodeBytes},
                        null,
                        null
                )
        );

        String expected = "0x71f907bf802b2a94000000000000000000000000000000000000800680b8a41415dae2000000000000000000000000000000000000000000000000000000000000000000379c09b5568d43b0ac6533a2672ee836815530b412f082f0b2e69915aa50fc00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000082010e94000000000000000000000000000000000000000080f906e3b906e00000002b04000041000000000141016f0000002c0400004100000000001403760000002d010000410000000000210376000000000130004c000000090000613d00a5000a0000034f00a5001f0000034f0000008001000039000000400200003900000000001203760000000001000357000000000110004c0000001d0000c13d0000002d010000410000000001010375000000000110004c000000180000c13d00000080010000390000000002000019000000000300001900a500960000034f0000002001000039000000000010037600000000000103760000002e01000041000000a6000103700000000001000019000000a70001037200010000000000020000008006000039000000400500003900000000006503760000002d010000410000000001010375000000040110008c0000005a0000413d0000002c01000041000000000101037500000000010103770000002f02000041000000000121016f000000300210009c000000440000c13d0000000001000357000000000110004c0000005c0000c13d0000002d010000410000000001010375000000040110008a000000010200008a0000003203000041000000000221004b00000000020000190000000002032019000000000131016f000000000431013f000000320110009c00000000010000190000000001034019000000320340009c000000000102c019000000000110004c0000005e0000c13d0000000001000019000000a700010372000000310110009c0000005a0000c13d0000000001000357000000000110004c000000650000c13d0000002d010000410000000001010375000000040110008a00000032020000410000001f0310008c00000000030000190000000003022019000000000121016f000000000410004c0000000002008019000000320110009c00000000010300190000000001026019000000000110004c000000670000c13d0000000001000019000000a7000103720000000001000019000000a7000103720000000001000019000000a7000103720000000001000019000100000006001d00a5008b0000034f000000010200002900000000001203760000003401000041000000a6000103700000000001000019000000a7000103720000002c01000041000000000101037500000004011000390000000001010377000100000005001d00a500720000034f000000010100002900000000010103750000003302000041000000000121016f000000a6000103700002000000000002000000010200008a000100000001001d000000000121013f000200000001001d000000000100001900a5008b0000034f0000000202000029000000000221004b000000820000213d00000001020000290000000001210019000000000200001900a500890000034f0000000200000005000000000001036f000000350100004100000000001003760000001101000039000000040200003900000000001203760000003601000041000000a700010372000000000012035b000000000001036f0000000001010359000000000001036f000000000401037500000000043401cf000000000434022f0000010003300089000000000232022f00000000023201cf000000000242019f0000000000210376000000000001036f0000000504300270000000000540004c0000009e0000613d00000000002103760000002001100039000000010440008a000000000540004c000000990000c13d0000001f0330018f000000000430004c000000a40000613d000000030330021000a5008d0000034f000000000001036f000000000001036f000000a500000374000000a600010370000000a700010372000000000000e001000000000000e001000000000000e001000000000000e0010000000000000000000000000000000000000000000000000000000000ffffff0000000000000000000000000000000000000000000000000000000000ffffe00000000000000000000000000000000000000000000000000000000000ffffc00000000000000000000000000000000000000000000000400000000000000000ffffffff000000000000000000000000000000000000000000000000000000006d4ce63c000000000000000000000000000000000000000000000000000000007cf5dab0000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ffffffffffffffff00000000000000000000000000000000000000000000002000000000000000804e487b71000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000240000000000000000c0";

        assertEquals(expected, Numeric.toHexString(TransactionEncoder.encode(transaction, null)));
    }

    @Test
    public void testEncodeExecute() {
        String calldata = FunctionEncoder.encode(CounterContract.encodeIncrement(BigInteger.valueOf(42L)));

        Transaction712 transaction = new Transaction712(
                BigInteger.ZERO,
                GAS_PRICE,
                GAS_LIMIT,
                "0xe1fab3efd74a77c23b426c302d96372140ff7d0c",
                BigInteger.ZERO,
                calldata,
                CHAIN_ID,
                new Eip712Meta(
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        null,
                        null,
                        null
                )
        );

        String expected = "0x71f859802b2a94e1fab3efd74a77c23b426c302d96372140ff7d0c80a47cf5dab0000000000000000000000000000000000000000000000000000000000000002a82010e94000000000000000000000000000000000000000080c0c0";

        assertEquals(expected, Numeric.toHexString(TransactionEncoder.encode(transaction, null)));
    }

}
