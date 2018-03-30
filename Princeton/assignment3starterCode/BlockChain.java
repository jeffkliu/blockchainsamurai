// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;
import java.util.Map;
import static java.util.stream.Collectors.toSet;
//import com.google.common.base.Optional;


public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool txPool;
    private UTXOPool utxo_pool;
    private TxHandler validTx;
    private HashMap<ByteArrayWrapper, Integer> storeBlockHeight;
    private HashMap<ByteArrayWrapper, Block> storeBlock;
    private HashMap<ByteArrayWrapper, UTXOPool> storeMaxUTXOPool;
    private Set<ByteArrayWrapper> oldestForkedBlock;
    private Block genesis_block;
    private int maxHeight;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        genesis_block = genesisBlock;
        txPool = new TransactionPool();
        storeBlockHeight = new HashMap<ByteArrayWrapper, Integer>();
        storeBlock = new HashMap<ByteArrayWrapper,Block>();
        utxo_pool = new UTXOPool();
        validTx = new TxHandler(utxo_pool);
        oldestForkedBlock = new HashSet<ByteArrayWrapper>();

        /*
        System.out.println("Getting genesis_block coinbase: " + genesisBlock.getCoinbase().getHash() + 
            "\nGetting hash of genesis_block: " + genesisBlock.getHash() + 
            "\nGetting previous hash of genesis_block: " + genesisBlock.getPrevBlockHash());
        */


        utxo_pool.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(),0), genesisBlock.getCoinbase().getOutput(0));

        storeBlock.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisBlock);
        storeBlockHeight.put(new ByteArrayWrapper(genesisBlock.getHash()), 1);
        maxHeight= storeBlockHeight.get(new ByteArrayWrapper(getMaxHeightBlock().getHash()));
        //initailize txhandler
        storeMaxUTXOPool = new HashMap<ByteArrayWrapper, UTXOPool>();
        storeMaxUTXOPool.put(new ByteArrayWrapper(genesisBlock.getHash()), utxo_pool);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        ByteArrayWrapper maxBlockHash = storeBlockHeight.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > 
            entry2.getValue() ? 1 : -1).get().getKey();

        int maxHeight = 0;
        ByteArrayWrapper theChosenOne = maxBlockHash;

        for(Map.Entry<ByteArrayWrapper, Integer> truth : storeBlockHeight.entrySet()){
            if(truth.getValue() > maxHeight){
                if(!oldestForkedBlock.contains(truth.getKey())){
                    maxHeight = truth.getValue();
                    theChosenOne = truth.getKey();
                }
                //System.out.println("This si the size of maxHeight: " + maxHeight);
            }
        }

        //System.out.println("This is the chosen one: " + theChosenOne);

        return storeBlock.get(theChosenOne);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return storeMaxUTXOPool.get(new ByteArrayWrapper(getMaxHeightBlock().getHash()));
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {

        boolean addFlag = true;

        if(block == null)
            return false;

        // Check if block claims to be genesis block
        if(block.getPrevBlockHash()==null)
            return false;

        if(maxHeight>CUT_OFF_AGE+1 && block.getPrevBlockHash() == genesis_block.getHash())
            return false;


        ByteArrayWrapper newBlock = new ByteArrayWrapper(block.getHash());
        storeBlock.put(newBlock, block);


//////////////////////////////////////////////////////////////////////////////////////////////////////

        Transaction[] txArray = block.getTransactions().toArray(new Transaction[block.getTransactions().size()]);


        for(Transaction tx: block.getTransactions())
            txPool.addTransaction(tx);


        if(getMaxHeightBlock().getHash() == block.getPrevBlockHash())
        {

            UTXOPool tmpPool = new UTXOPool(storeMaxUTXOPool.get(new ByteArrayWrapper(
                getMaxHeightBlock().getHash())));
            validTx = new TxHandler(tmpPool);

            Transaction[] validArray = validTx.handleTxs(txArray);
            if(txArray.length != validArray.length)
                return false;

            tmpPool = validTx.getUTXOPool();
            tmpPool.addUTXO(new UTXO(block.getCoinbase().getHash(),0), block.getCoinbase().getOutput(0));
            storeBlockHeight.put(newBlock, maxHeight + 1);
            storeMaxUTXOPool.put(newBlock, tmpPool);
        }
        else if(storeBlockHeight.containsKey(new ByteArrayWrapper(block.getPrevBlockHash())))
        {
            //System.out.println("This is the side chain");
            int currentHeight = storeBlockHeight.get(new ByteArrayWrapper(block.getPrevBlockHash())) + 1;
            UTXOPool tmpPool;

            if(currentHeight<maxHeight- CUT_OFF_AGE + 1)
                return false;

            if (currentHeight == maxHeight)
                 oldestForkedBlock.add(newBlock);


            tmpPool = new UTXOPool(storeMaxUTXOPool.get(new ByteArrayWrapper(block.getPrevBlockHash())));

            validTx = new TxHandler(tmpPool);
            Transaction[] validArray = validTx.handleTxs(txArray);
            if(txArray.length != validArray.length)
                return false;
            storeBlockHeight.put(newBlock, currentHeight);
            tmpPool = validTx.getUTXOPool();
            tmpPool.addUTXO(new UTXO(block.getCoinbase().getHash(),0), block.getCoinbase().getOutput(0));

            storeMaxUTXOPool.put(newBlock, tmpPool);
        }   
        else
            return false;



        maxHeight = storeBlockHeight.get(new ByteArrayWrapper(getMaxHeightBlock().getHash()));

        //System.out.println("This is the size of maxHeight: " + maxHeight);


        for(Transaction tx: txPool.getTransactions())
            txPool.removeTransaction(tx.getHash());


        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.txPool.addTransaction(tx);
    }

    public static void main(String[] args)
    {
        //Nothing yet
    }
}