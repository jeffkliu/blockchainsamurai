import java.util.ArrayList;
import java.util.Set;
import java.util.*;
import static java.util.stream.Collectors.toSet;


/*
        a. New transactions are broadcasted to all nodes
        b. Each node collects new transactions into a block
        c. In each round a random node gets to broadcast its block
        d. Other nodes accept the block only if all transactions in it are valid (unspent, valid sigs)
        e. Nodes express their accepance of the block by including its hash in the next block they create

*/

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    public int rounds;
    public boolean[] in_followees;
    public Set<Transaction> in_pendingTransactions;
    private HashMap<Node, ArrayList<Integer>> checkNode;
    private HashMap<Integer, ArrayList<Integer>> senderPair;
    private Node[] nodeList;
    private boolean[] blackListed;



    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.rounds = numRounds;
        in_pendingTransactions = new HashSet<Transaction>();

        if (senderPair == null)
            senderPair = new HashMap<Integer, ArrayList<Integer>>();
    
        if(checkNode == null)
            checkNode = new HashMap<Node, ArrayList<Integer>> ();    
     }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        System.out.println(Arrays.toString(followees));
        this.in_followees = followees;
        System.out.println(Arrays.toString(nodeList) +"\n");

        }


    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS

        for(Transaction tx:pendingTransactions){
            this.in_pendingTransactions.add(tx);

        }

        if(in_followees.length == nodeList.length)
            {
                for(int i = 0;i<nodeList.length;i++)
                {
                    if(in_followees[i])
                    {
                        if(!checkNode.containsKey(nodeList[i])){
                            checkNode.put(nodeList[i], new ArrayList<Integer>());
                        }
                        checkNode.get(nodeList[i]).add(nodeList[i].sendToFollowers().size());
                    }
                }
            }


        for(Map.Entry<Node, ArrayList<Integer>> truth : checkNode.entrySet()){
            System.out.println("This is the key: " + truth.getKey() + "\nThis is size: " + truth.getValue() );

        }

        //System.out.println("\n\nSize before: " + this.getPending());
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS


        Set<Transaction> toSend = new HashSet<>(in_pendingTransactions);
        in_pendingTransactions.clear();

        return toSend;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS

        System.out.println("\n\n\nNEW NODE TESTING TIME!!\n\n\n");

        Set<Integer> senders = candidates.stream().map(c -> c.sender).collect(toSet());
        for (int i = 0; i < in_followees.length; i++) {
            if (in_followees[i] && !senders.contains(i))
                blackListed[i] = true;
        }

        for(Candidate c: candidates) {
            //System.out.println("This is tx.id: " + c.tx.id + "\nTransaction: " + c.sender +"\n");
            if (!blackListed[c.sender]) {
                in_pendingTransactions.add(c.tx);
            }

            if(!senderPair.containsKey(c.sender))
                senderPair.put(c.sender, new ArrayList<Integer>());
            senderPair.get(c.sender).add(c.tx.id);

        }

        /*
        for(Map.Entry<Integer, ArrayList<Integer>> truth : senderPair.entrySet()){
            System.out.println("This is the senderNode index: " + truth.getKey() + "\nThis iese are transactions: " + truth.getValue() );
        }

        System.out.println("\n\nSize after: " + this.getPending());
        */
    }

    public void storeNodes(HashMap<Node, ArrayList<Integer>>  hashcandidate){

        checkNode = new HashMap<Node, ArrayList<Integer>> (hashcandidate);


    }

    public void storeSenderPAirs(HashMap<Integer, ArrayList<Integer>>  hashcandidate){

        senderPair = new HashMap<Integer, ArrayList<Integer>> (hashcandidate);


    }

    public void saveNodes(Node[] nodes)
    {
        //System.out.println("Saved the nodes thothotho");
        nodeList = nodes;
    }

    public int getPending()
    {
        return this.in_pendingTransactions.size();
    }

}
