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
                System.out.println("There is a negative value");
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
                System.out.println("This input does not exist");
                return false;
            }
            else
            {
                currValue = currValue + newPool.getTxOutput(new UTXO(prevTransactionHash, oIndex)).value;
                currSender = newPool.getTxOutput(new UTXO(prevTransactionHash, oIndex)).address;
                testDup.addUTXO(new UTXO(prevTransactionHash, oIndex), newPool.getTxOutput(new UTXO(prevTransactionHash, oIndex)));
        
            }

            if(verify.verifySignature(newPool.getTxOutput(new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex)).address
                , tx.getRawDataToSign(j), currInput.signature) == false){
                System.out.println("Fake signature");
                return false;
            }
        }

        if(testDup.getAllUTXO().size() < tx.numInputs()){
            System.out.println("Double-spending attempt");
            return false;
        }

        if(currValue < accumulatedValue)
            {
                System.out.println("Output is greater than input");
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
        ArrayList<Transaction> invalidTx = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        ArrayList<Transaction> validTx = new ArrayList<Transaction>();
        ArrayList<Transaction> tempTxArray = new ArrayList<Transaction>();
        ArrayList<UTXO> allCurrentUTXOs = new ArrayList<UTXO>();
        boolean new_valid_transaction = false;

        for(int i = 0; i < invalidTx.size(); i++)
        {
            if(this.isValidTx(invalidTx.get(i)))
            {
                //there's still valid tx
                new_valid_transaction = true;
                //deleting older input
                for(int a = 0; a < invalidTx.get(i).numInputs(); a++)
                {
                    byte[] prevTransactionHash = invalidTx.get(i).getInput(a).prevTxHash;
                    int oIndex = invalidTx.get(i).getInput(a).outputIndex;

                     if(newPool.contains(new UTXO(prevTransactionHash,oIndex)))
                        newPool.removeUTXO(new UTXO(prevTransactionHash,oIndex));
                }

                //Adding all new UTXOs
                for(int j = 0; j < invalidTx.get(i).numOutputs(); j++)
                {
                    newPool.addUTXO(new UTXO(invalidTx.get(i).getHash(), j), invalidTx.get(i).getOutput(j));   
                }

                validTx.add(invalidTx.get(i));
            }
            else
            {
                tempTxArray.add(invalidTx.get(i));
            }
        }


        return validTx.toArray(new Transaction[validTx.size()]);
    }

    /* Get UTXO Pool */
    public UTXOPool getUTXOPool(){
        return this.newPool;
    }
    
    public static void main(String[] arg)
    {
    		System.out.println("Hello");
    }
    
}
