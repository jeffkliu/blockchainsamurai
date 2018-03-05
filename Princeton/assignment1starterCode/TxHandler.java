import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.*;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    //UTXOPool accessed by this class
    private UTXOPool newPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        newPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // Checking to see if there is a hash in tx hash

        ArrayList<Transaction.Input> allInputs = tx.getInputs();
        ArrayList<Transaction.Output> allOutputs = tx.getOutputs();
        ArrayList<UTXO> allCurrentUTXOs = newPool.getAllUTXO();
        //This is used to verify signatures
        Crypto verify = new Crypto();
        double accumulatedValue = 0;
        double currValue = 0;

        for(int i = 0; i < tx.numOutputs();i++)
        {
            if(tx.getOutput(i).value < 0 )
            {
                //System.out.println("There is a negative value");
                return false;
            }
            accumulatedValue = accumulatedValue + tx.getOutput(i).value;

        }

        //System.out.println("Input size " + tx.numInputs());

        UTXOPool testDup = new UTXOPool();
        for(int j = 0; j < tx.numInputs(); j++)
        {
            byte[] prevTransactionHash = tx.getInput(j).prevTxHash;
            int oIndex = tx.getInput(j).outputIndex;

            PublicKey currSender = null;
            Transaction.Input currInput = tx.getInput(j);



            if(newPool.contains(new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex))==false)
            {
                //System.out.println("This input does not exist");
                return false;
            }
            else
            {
                for(int i = 0; i <allCurrentUTXOs.size();i++)
                {

                    if(Arrays.equals(allCurrentUTXOs.get(i).getTxHash(),prevTransactionHash) && 
                        allCurrentUTXOs.get(i).getIndex() == oIndex)
                        {
                            currValue = currValue + newPool.getTxOutput(allCurrentUTXOs.get(i)).value;
                            currSender = newPool.getTxOutput(allCurrentUTXOs.get(i)).address;
                            testDup.addUTXO(new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex), 
                            newPool.getTxOutput(allCurrentUTXOs.get(i)));
                        }
                }
        
            }

            if(verify.verifySignature(newPool.getTxOutput(new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex)).address
                , tx.getRawDataToSign(j), currInput.signature) == false){
                System.out.println("Fake signature");
                return false;
            }
        }

        if(testDup.getAllUTXO().size() < tx.numInputs()){
            System.out.println("FAKENEWS");
            return false;
        }

        if(currValue < accumulatedValue)
            {
                return false;
            }

                
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Transaction[] newTX = possibleTxs;
        return newTX;
    }
    
    public static void main(String[] arg)
    {
    		System.out.println("Hello");
    }
    

/*
    // Using this main method to test this
    public static void main(String[] arg) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        //creating new UTXO pool for testing
        UTXOPool newP = new UTXOPool();
        HashMap<PublicKey, String> hashM = new HashMap<PublicKey, String>();

        //initializing txhandler to validate transactions to prevent fraud i guess
        TxHandler testHandler = new TxHandler(newP);

        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice   = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_goofy = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        //System.out.println(pk_alice.getPrivate().toString());

        //Getting Private Key of all the players
        PrivateKey scrooge_privKey = pk_scrooge.getPrivate();
        PrivateKey alice_privKey = pk_alice.getPrivate();
        PrivateKey goofy_privKey = pk_goofy.getPrivate();

        //Getting Public Key of all players
        PublicKey scrooge_pubKey = pk_scrooge.getPublic(); hashM.put(scrooge_pubKey, "Scrooge");
        PublicKey alice_pubKey = pk_alice.getPublic(); hashM.put(alice_pubKey, "Alice");
        PublicKey goofy_pubKey = pk_goofy.getPublic(); hashM.put(goofy_pubKey, "Goofy");


        //genesisHash
        byte[] genesisHash = BigInteger.valueOf(0).toByteArray();

        //test transaction BRUTE FORCE STYLE. This is the genesis block.
        Transaction genesisTX = new Transaction();
        genesisTX.addInput(genesisHash, 0);
        //new Hash for signing purposes and creating genesis block
        byte[] initialHash = BigInteger.valueOf(0).toByteArray();
        //setting hash of newScroogeTX
        genesisTX.setHash(initialHash);
  
        genesisTX.addSignature(genesisTX.createSig(0, scrooge_privKey), 0);


        //note this is a genesis block so scrooge was first recipient
        genesisTX.addOutput(100, scrooge_pubKey);
        UTXO genesisOutput = new UTXO(genesisTX.getHash(), 0);

        newP.addUTXO(genesisOutput, genesisTX.getOutput(0));

        Transaction StoA_tx = new Transaction();
        byte[] secondHash = BigInteger.valueOf(0).toByteArray();
        StoA_tx.setHash(secondHash);

        //UTXO firstUTXO;
        //Creating new transaction to pay from Scrooge to Alice. Make sure we confirm genesis first

        if(newP.getAllUTXO().size()==1){
            StoA_tx.addInput(genesisTX.getHash(),0);
        }
        else{
            System.out.println("Make sure you have the genesis block");
        }

        double diff = newP.getTxOutput(newP.getAllUTXO().get(0)).value;
 
        //testHandler.isValidTx(genesisTX);

        StoA_tx.addOutput(40, alice_pubKey);
        StoA_tx.addOutput(15, goofy_pubKey);

        PublicKey currSender = null;

        //used to parse Input and get public address of current Sender
        for(int i = 0; i < StoA_tx.numInputs(); i++)
        {
            byte[] prevTransactionHash = StoA_tx.getInput(i).prevTxHash;
            int oIndex = StoA_tx.getInput(i).outputIndex;

            for(int j = 0; j < newP.getAllUTXO().size(); j++)
            {
                if(Arrays.equals(newP.getAllUTXO().get(j).getTxHash(),prevTransactionHash) && 
                    newP.getAllUTXO().get(j).getIndex() == oIndex)
                    {
                        currSender = newP.getTxOutput(newP.getAllUTXO().get(j)).address;
                    }
            }
        }


        for(int i = 0; i < StoA_tx.numOutputs();i++)
        {
            if(diff >= 0)
                diff = diff - StoA_tx.getOutput(i).value;
            if (diff != 0 && i == StoA_tx.numOutputs()-1)
            {
                StoA_tx.addOutput(diff, currSender);
            }
            newP.addUTXO(new UTXO(StoA_tx.getHash(), i), StoA_tx.getOutput(i));
        }


        for(int i = 0; i < newP.getAllUTXO().size(); i++)
        {
            System.out.println("Value: " + newP.getTxOutput((newP.getAllUTXO().get(i))).value);
            System.out.println("Current Address Owner: " + hashM.get(newP.getTxOutput(newP.getAllUTXO().get(i)).address)) ;
        }

        testHandler.isValidTx(StoA_tx);

        byte[] thirdHash = BigInteger.valueOf(0).toByteArray();

    }
    */
}
