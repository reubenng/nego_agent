import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * MyAgent returns the bid that maximises its own utility if it is the first to make offer.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 */
@SuppressWarnings("serial")
public class MyAgent extends AbstractNegotiationParty {
    private final String description = "Group 33 Agent";

    private int round = 0; // number of round
    private Bid lastReceivedOffer = null; // offer on the table
    private Bid myLastOffer = null;
    
    public Float[][] probMatrix; // probability matrix
    public Float[][] prob2Matrix; // normalised squared probability matrix
    public List<Issue> issues;
    public IssueDiscrete issueDiscrete;
    public NegotiationInfo info_2;   
    
    public int power = 2; // power for probability
    
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        
        info_2 = info;
        
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();

        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        issues = additiveUtilitySpace.getDomain().getIssues(); // list of issues
        Float[] weights = new Float[issues.size()];
        
        String[][] valueNameMatrix = new String[issues.size()][]; // matrix for storing value names
        Float[][] valueMatrix = new Float[issues.size()][]; // matrix for storing value evaluations

        probMatrix = new Float[issues.size()][]; // matrix for probability
        prob2Matrix = new Float[issues.size()][]; // matrix for normalised squared probability
        
        int i = 0;
        
        for (Issue issue : issues) {
            int issueNumber = issue.getNumber();
            Float weight = (float) additiveUtilitySpace.getWeight(issueNumber);
            weights[i] = weight;
//            System.out.println(">> " + issue.getName() + " weight: " + weight);

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);
            
            int j = 0;
            String[] NameArray = new String[issueDiscrete.getValues().size()];
            Float[] valueArray = new Float[issueDiscrete.getValues().size()];
            
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
                	valueArray[j] = (float) evaluatorDiscrete.getValue(valueDiscrete); // put value into array
                	valueArray[j] = (float) Math.pow(valueArray[j], 1 + 4 * weights[i]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                j++;
            }
            valueNameMatrix[i] = NameArray; // put value name arrays into name matrix
            valueMatrix[i] = valueArray; // put value arrays into value matrix
            Float valueSums = (float) 0;
            for (Float a : valueArray) // find sum of values in an issue
            	valueSums += a;
            Float smallestProb =  1/valueSums; // find probability of the smallest unit
//            System.out.println("smallestProb: " + smallestProb);
            
            j = 0;
            Float[] probArray = new Float[issueDiscrete.getValues().size()]; // array of probability
            Float[] prob2Array = new Float[issueDiscrete.getValues().size()]; // squared array then normalised array
            // create array of cumulative probability
            for (@SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
            	if (j == 0){
            		probArray[j] = smallestProb * valueArray[j];
            		prob2Array[j] = (float) Math.pow(probArray[j], power); // find squared array
//                    System.out.println("probArray " + j + ": "+ probArray[j]);
            	} else {
            		probArray[j] = smallestProb * valueArray[j];
//            		probArray[j] = smallestProb * valueArray[j] + probArray[j-1]; // not used, for cumulative probability
            		prob2Array[j] = (float) Math.pow(probArray[j], power);
//                    System.out.println("probArray " + j + ": "+ probArray[j]);
            	}
            	j++;
            }
            Float sqSums = (float) 0;
            for (Float a : prob2Array) // find sum of values in an issue
            	sqSums += a;
            j = 0;
            for (@SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) { // normalised probability
            	prob2Array[j] = prob2Array[j]/sqSums;
            	j++;
            }
            probMatrix[i] = probArray;
            prob2Matrix[i] = prob2Array;
            i++;
        }
        System.out.println("Issues: " + issues);
        System.out.println("Issue weights: " + Arrays.toString(weights));
        System.out.println("All value names: " + Arrays.deepToString(valueNameMatrix));
        System.out.println("All values: " + Arrays.deepToString(valueMatrix));
        System.out.println("Probability matrix: " + Arrays.deepToString(probMatrix));
        System.out.println("Norm Sq Prob matrix: " + Arrays.deepToString(prob2Matrix));
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
        Float time = (float) getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
                                               // The time is normalised, so agents need not be
                                               // concerned with the actual internal clock.

    	System.out.println("time: "+ time);
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
                myLastOffer = getBidFromRoulette(probMatrix, prob2Matrix, time);
//                myLastOffer = generateRandomBid();
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

    public Bid getBidFromRoulette(Float[][] origProbMatrix, Float[][] normProbMatrix, double[] freqMatrix, Float time) {

        // 1) get number of issues (rows) and values (columns) from the profile variable
        int NumberOfIssues = origProbMatrix.length;
        int NumberOfValues = origProbMatrix[0].length;
        Value[] picked_values_index = new Value[issues.size()];

        // 2) create vector (size=NumberOfIssues) of random numbers from 0 to 1
        Double[] randomArray = new Double[NumberOfIssues];
        double offset = 0;
        
        for(int issue_n = 0; issue_n < NumberOfIssues; issue_n++) {
            randomArray[issue_n] = Math.random();
        }

        // 3) Add Preference/Frequency function
        double[][] prefProbMatrix = new double[NumberOfIssues][NumberOfValues];
        double oldValue;
        double exponent;
        double i_freq;
        double[] valuesSum = new double[NumberOfIssues];

        for(int i_n = 0; i_n < NumberOfIssues; i_n++) {

            i_freq = freqMatrix[i_n];

            // change every value prob accordingly
            for(int v_n = 0; v_n < NumberOfValues; v_n++) {
                oldValue = normProbMatrix[i_n][v_n];
                exponent = 0.5 + (1 - i_freq);
                prefProbMatrix[i_n][v_n] = Math.pow(oldValue, exponent);

                valuesSum[i_n] += prefProbMatrix[i_n][v_n];
            }
        }

        // normalize and return
        for(int i_n = 0 ; i_n < NumberOfIssues; i_n++) {
            // change every value prob accordingly
            for(int v_n = 0; v_n < NumberOfValues; v_n++) {
                prefProbMatrix[i_n][v_n] = prefProbMatrix[i_n][v_n] / valuesSum[i_n];
            }
        }

        // 4) apply time dependent function
        Float[][] timeBiasedProbMatrix = new Float[issues.size()][];
        
        float originalValue;
        float normalizedValue;

        int i = 0;
        for(Issue issue : issues) {
        	int j = 0;
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            Float[] probtArray = new Float[issueDiscrete.getValues().size()]; // array of probability * t
            for(@SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                // originalValue = origProbMatrix[i][j];
                normalizedValue = normProbMatrix[i][j];
                prefValue = prefProbMatrix[i][j];
                // probtArray[j] = normalizedValue;
                // probtArray[j] = (originalValue * time * time) - (normalizedValue * (time * time - 1));
                probtArray[j] = (prefValue * time * time) - (normalizedValue * (time * time - 1));
                j++;
            }
            timeBiasedProbMatrix[i] = probtArray;
            i++;
        }
        
        // 5) apply roulette selection
        i = 0;
        for(Issue issue : issues) {

            // clear offset
            offset = timeBiasedProbMatrix[i][0];

            int j = 0;

            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            for(ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                
                // if it falls in the pie section store the value from the profile map
                if (randomArray[i] < offset) {
                    picked_values_index[i] = valueDiscrete;
                    break;

                // else increment offset by the appropriate value
                } else {
                    offset = offset + timeBiasedProbMatrix[i][j + 1];
                }
                j++;
            }
            i++;
        }

        // 6) Generate new bid with chosen values
        HashMap<Integer, Value> issueMap = new HashMap<>();
        int issue_n = 0;
        for (Issue issue : issues) {
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            List<ValueDiscrete> discreteValues = new ArrayList<>();
            discreteValues.addAll(issueDiscrete.getValues());
            issueMap.put(issue.getNumber(), picked_values_index[issue_n]);
            issue_n++;
        }
        
        Bid newBid = new Bid(info_2.getUtilitySpace().getDomain(), issueMap);

        // Debug
        System.out.println("Time-biased prob matrix: " + Arrays.deepToString(timeBiasedProbMatrix));
        System.out.println("Picked index after roullete: " + Arrays.toString(picked_values_index));

        return newBid;
    }
}
