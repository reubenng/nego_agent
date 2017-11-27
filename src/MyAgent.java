import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * MyAgent returns the bid that maximizes its own utility if it is the first to make offer.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 */
@SuppressWarnings("serial")
public class MyAgent extends AbstractNegotiationParty {
    private final String description = "Group 33 Agent";

    private int round = 0; // number of round
    private Bid lastReceivedOffer = null; // offer on the table
    private Bid myLastOffer = null;
        
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();

        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues(); // list of issues
        Double[] weights = new Double[issues.size()];
        
        String[][] valueNameMatrix = new String[issues.size()][]; // matrix for storing value names
        int[][] valueMatrix = new int[issues.size()][]; // matrix for storing value evaluations

        Float[][] probMatrix = new Float[issues.size()][]; // matrix for storing value evaluations
        
        int i = 0;
        
        for (Issue issue : issues) {
            int issueNumber = issue.getNumber();
            double weight = additiveUtilitySpace.getWeight(issueNumber);
            weights[i] = weight;
//            System.out.println(">> " + issue.getName() + " weight: " + weight);

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);
            
            int j = 0;
            String[] NameArray = new String[issueDiscrete.getValues().size()];
            int[] valueArray = new int[issueDiscrete.getValues().size()];
            
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
//                System.out.println(valueDiscrete.getValue());
                NameArray[j] = (String) valueDiscrete.getValue(); // put value names into array
                
//                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
                try {
//					System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                try {
                	valueArray[j] = evaluatorDiscrete.getValue(valueDiscrete); // put value into array
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                j++;
            }
            valueNameMatrix[i] = NameArray; // put value name arrays into name matrix
            valueMatrix[i] = valueArray; // put value arrays into value matrix
            Float valueSums = (float) IntStream.of(valueArray).sum(); // find sum of values in an issue
            Float smallestProb =  1/valueSums; // find probability of the smallest unit
//            System.out.println("smallestProb: " + smallestProb);
            
            j = 0;
            Float[] probArray = new Float[issueDiscrete.getValues().size()]; // array of linear probability
            // create array of linear probability
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
            	if (j == 0){
            		probArray[j] = smallestProb * valueArray[j];
//                    System.out.println("probArray " + j + ": "+ probArray[j]);
            	} else {
            		probArray[j] = smallestProb * valueArray[j] + probArray[j-1];
//                    System.out.println("probArray " + j + ": "+ probArray[j]);
            	}
            	j++;
            }
            probMatrix[i] = probArray;
            i++;
        }
        System.out.println("Issues: " + issues);
        System.out.println("Issue weights: " + Arrays.toString(weights));
        System.out.println("All value names: " + Arrays.deepToString(valueNameMatrix));
        System.out.println("All values: " + Arrays.deepToString(valueMatrix));
        System.out.println("Probability matrix: " + Arrays.deepToString(probMatrix));

    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
    	round++;
    	System.out.println("Round "+ round);
    	
        // According to Stacked Alternating Offers Protocol list includes
        // Accept, Offer and EndNegotiation actions only.
        double time = getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
                                               // The time is normalized, so agents need not be
                                               // concerned with the actual internal clock.

        // If first agent, no offer on the table yet, do max utility bid
        if (lastReceivedOffer == null || !list.contains(Accept.class)){
        	System.out.println("First offer");
        	return new Offer(this.getPartyId(), this.getMaxUtilityBid());
        } else {
            // Accepts the bid on the table in this phase,
            // if the utility of the bid is higher than Agent's last bid.
        	if (lastReceivedOffer != null
                && myLastOffer != null
                && this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(myLastOffer)){
        		System.out.println("Accept offer");
        		return new Accept(this.getPartyId(), lastReceivedOffer);
        	} else {
            	// otherwise do strategy counter offer
        		System.out.println("Make offer");
                myLastOffer = generateRandomBid();
                return new Offer(this.getPartyId(), myLastOffer);
        	}
        }
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();
        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
