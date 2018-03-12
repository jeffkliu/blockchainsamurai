import java.util.ArrayList;
import java.util.Set;
import java.util.*;

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



    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.rounds = numRounds;
        in_pendingTransactions = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.in_followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS

        for(Transaction tx:pendingTransactions)
            this.in_pendingTransactions.add(tx);
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        
        return this.in_pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS

        System.out.println("Size of candidates: " + candidates.size());

        for(Candidate c: candidates) {
            System.out.println("This is candiate: " + c + "\nTransaction: " + c.tx.id +"\n");
            this.in_pendingTransactions.add(c.tx);
    }
}
}
