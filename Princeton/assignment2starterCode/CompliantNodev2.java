import java.util.HashSet;
import java.util.Set;
import java.util.*;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_tXDistribution;
    private int numRounds;

    private boolean[] followees;

    private Set<Transaction> pendingTransactions;
    private Set<Transaction> tmpTransactions;
    private Set<Integer> evens;
    private Set<Integer> odds;

    private boolean[] blackListed;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_tXDistribution = p_txDistribution;
        this.numRounds = numRounds ;
        this.evens = new HashSet<Integer>();
        this.odds = new HashSet<Integer>();     
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.blackListed = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {

        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> toSend = new HashSet<>(pendingTransactions);

        tmpTransactions = toSend;
        pendingTransactions.clear();
        return toSend;
    }

    public Set<Integer> storeEvens(Set<Integer> carryOverEvens)
    {
        evens = carryOverEvens;
    }

     public Set<Integer> storeOdds(Set<Integer> carryOverOdds)
    {
        odds = carryOverOdds;
    }


    public void receiveFromFollowees(Set<Candidate> candidates) {

        numRounds-=1;

        Set<Integer> senders = candidates.stream().map(c -> c.sender).collect(toSet());
        Set<Transaction> tx = candidates.stream().map(c -> c.tx).collect(toSet());


        System.out.println("\nWe are at round: " + (11 - numRounds) + "\n");
        System.out.println("This is the size of pendingTransactions: " + tmpTransactions.size());


        HashMap<Integer, ArrayList<Transaction> > checkSize = new HashMap<Integer, ArrayList<Transaction>>();



        System.out.println("\n\nThese are the followees: " + Arrays.toString(followees));
        System.out.println(senders);

        HashMap<Integer, Boolean> oddBehavior = new HashMap<Integer, Boolean>();

        for (int i = 0; i < followees.length; i++) {
            if (followees[i] && !senders.contains(i))
                blackListed[i] = true;


        }

        for(Candidate c : candidates)
        {
            if(!checkSize.containsKey(c.sender))
                checkSize.put(c.sender, new ArrayList<Transaction>());
            
            checkSize.get(c.sender).add(c.tx);

            if(numRounds == 1 && !tmpTransactions.contains(c.tx))
                blackListed[c.sender]= true;
        }

        HashMap<Integer, Boolean> storeFlags = new HashMap<Integer, Boolean>();
        int sizeOfArray = tmpTransactions.size();

        Iterator<Integer> checkSenders = senders.iterator();
        ArrayList<Integer> byebye = new ArrayList<Integer>();

        while(checkSenders.hasNext())
        {
            Integer tmpInt = checkSenders.next();

            if(numRounds%2 == 0)
                evens.add(tmpInt);
            else
                odds.add(tmpInt);

            if(checkSize.get(tmpInt).size() != tmpTransactions.size()){
                storeFlags.put(tmpInt,false);
                byebye.add(tmpInt);
            }
            else
                storeFlags.put(tmpInt,true);
        }

        Collection<Boolean> boolFlags = storeFlags.values();

        int trueTotal = 0;
        int falseTotal = 0;

        for(Boolean check: boolFlags)
        {
            if(check)
                trueTotal+=1;
            else
                falseTotal+=1;
        }

        Iterator<Integer> byebyeiter = byebye.iterator();

        System.out.println(((double)2/8));

        if((((double)falseTotal/trueTotal) > 20.0) && (numRounds == 2))
        {
            while(byebyeiter.hasNext())
                blackListed[byebyeiter.next()] = false;
        }


       for (Candidate c : candidates) {


            if (!blackListed[c.sender]) 
                pendingTransactions.add(c.tx);

        }


        this.storeEvens(evens);
        this.storeOdds(odds);

        System.out.println("This is the blacklist: " + Arrays.toString(blackListed));



       // System.out.println("This size of pendingTransactions after the fact: " + tx.size());

    }
}