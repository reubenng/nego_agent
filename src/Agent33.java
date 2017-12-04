package group33;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Agent33 extends AbstractNegotiationParty {
    private final String description = "Group 33 Agent";

    private int round = 0; // number of round
    private Bid lastReceivedOffer = null; // offer on the table
    private Bid myLastOffer = null;
    private AgentID lastsender;
    private AgentID previoussender;
    private AgentID[] senders = new AgentID[2];
    
    public Float[][] probMatrix; // probability matrix
    public Float[][] prob2Matrix; // normalised squared probability matrix
    public List<Issue> issues;
    public IssueDiscrete issueDiscrete;
    public NegotiationInfo info_2;   

    public ArrayList<Value> bidlist; // matrix for storing opponent1's bids
    public ArrayList<Value> bidlist2; // matrix for storing opponent2's bids
    public int b = 0; // bid counter
    public int timeflag = 0;
    public Value[][] bidmatrix; // bid array matrix
    public Float[] valfreq; // value occurrence of opponent issues
    public String[][] valueNameMatrix;
    
    // parameters
    public double power = 2; // power for probability
//    public double lastpower = 1.5; // power for probability
    public int timepower = 1;
    public Float startnegotime = (float) 0.9; // strategy start time
    public Float worseStartTime = (float) 0.95;
    public Float worseExpo = (float) 0.8;
    
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

        bidlist = new ArrayList<Value>();
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
                NameArray[j] = valueDiscrete.getValue(); // put value names into array
                
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

    	System.out.println("Time: "+ time);
        // If first agent, no offer on the table yet, do max utility bid
        if (lastReceivedOffer == null || !list.contains(Accept.class)){
        	System.out.println("First offer");
        	return new Offer(this.getPartyId(), this.getMaxUtilityBid());
        } else {
        	if (time < startnegotime){ // bid collection phase
        	    HashMap<Integer, Value> valuelist = lastReceivedOffer.getValues();
                Value[] opvaluearray = new Value[issues.size()];
                // turn bid into array
                int a = 1;
                for(@SuppressWarnings("unused") Issue issue : issues) {
                   opvaluearray[a-1] = (Value) valuelist.get(a);
                   /*System.out.print("key is: "+ a + " & Value is: ");
                   System.out.println(valuelist.get(a));*/
                   a++;
                }
//                System.out.println("opvaluearray: " + Arrays.deepToString(opvaluearray));
                bidlist.addAll(Arrays.asList(opvaluearray));
            	b++;

            	return new Offer(this.getPartyId(), this.getMaxUtilityBid());
        		
        	} else {
        		if (timeflag == 0){
//                    System.out.println("b " + b);
        	        Value[][] bidmatrix = new Value[b][];
//                    System.out.println("bidlist " + bidlist);
                    for (int m = 0; m < b; m++){
            	        Value[] bidarray = new Value[issues.size()];
                    	for (int n = 0; n < issues.size(); n++){
                            bidarray[n] = bidlist.get(n + (issues.size() * m));
                    	}
                        bidmatrix[m] = bidarray;
                    }
//                    System.out.println("bidMatrix: " + Arrays.deepToString(bidmatrix));
                    valfreq = occurrence(bidmatrix);
        			timeflag = 1;
        		}
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
                    myLastOffer = getBidFromRoulette(probMatrix, prob2Matrix, valfreq, time);
//                    myLastOffer = generateRandomBid();
                    return new Offer(this.getPartyId(), myLastOffer);
            	}
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

            if (lastsender != null)
            	previoussender = lastsender;
//            System.out.println("previoussender: " + previoussender);
            // storing last received offer
            lastReceivedOffer = offer.getBid();

//          System.out.println("sender: " + sender);
            if (!Arrays.asList(senders).contains(sender) && senders[0] == null){ // (senders[0] != sender) && (senders[1] != sender)
            	senders[0] = sender;
            } else if (!(Arrays.asList(senders).contains(sender)) && senders[0] != null){ // senders[1] != sender
            	senders[1] = sender;
	        }

//            System.out.println("senders: " + Arrays.deepToString(senders));
            lastsender = sender;
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

    // roulette function
    public Bid getBidFromRoulette(Float[][] origProbMatrix, Float[][] normProbMatrix, Float[] valfreq2, Float time) {

        // 1) get number of issues (rows) and values (columns) from the profile variable
        int NumberOfIssues = origProbMatrix.length;
//        int NumberOfValues = origProbMatrix[0].length;
        Value[] picked_values_index = new Value[issues.size()];

        // 2) create vector (size=NumberOfIssues) of random numbers from 0 to 1
        Float[] randomArray = new Float[NumberOfIssues];
        Float offset = (float) 0.0;
        
        for(int issue_n = 0; issue_n < NumberOfIssues; issue_n++) {
            randomArray[issue_n] = (float) Math.random();
        }

        // 3) Add Preference/Frequency function
        Float[][] prefProbMatrix = new Float[NumberOfIssues][];
        Float[][] worsePrefProbMatrix = new Float[NumberOfIssues][];
        Float oldValue;
        Float exponent;
        Float i_freq;
        Float[] valuesSum = new Float[NumberOfIssues];
        Float[] worseValuesSum = new Float[NumberOfIssues];

        int i_n = 0;
        for( Issue issue : issues) {
            i_freq = valfreq2[i_n];

            int v_n = 0;
            // change every value prob accordingly
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            Float[] prefProbArray = new Float[issueDiscrete.getValues().size()];
            Float[] worsePrefProbArray  = new Float[issueDiscrete.getValues().size()];
            for(@SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                oldValue = normProbMatrix[i_n][v_n];
                exponent = (float) (0.5 + (1 - i_freq));
                prefProbArray[v_n] = (float) Math.pow(oldValue, exponent);
                worsePrefProbArray[v_n] = (float) Math.pow(prefProbArray[v_n], worseExpo);
                v_n++;
            }

            for(int sum_i = 0; sum_i < issueDiscrete.getValues().size(); sum_i++) {
                if (valuesSum[i_n] == null) {
                	valuesSum[i_n] = prefProbArray[sum_i];
                	worseValuesSum[i_n] = worsePrefProbArray[sum_i];
                } else {
                	valuesSum[i_n] = valuesSum[i_n] + prefProbArray[sum_i];
                	worseValuesSum[i_n] = worseValuesSum[i_n] + worsePrefProbArray[sum_i];
                }
            }

            prefProbMatrix[i_n] = prefProbArray;
            worsePrefProbMatrix[i_n] = worsePrefProbArray;
            i_n++;
        }

        // normalize and return
        i_n = 0 ;
        for( Issue issue : issues) {
            // change every value prob accordingly
        	int v_n = 0;
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            Float[] prefProbArray = new Float[issueDiscrete.getValues().size()];
            Float[] worsePrefProbArray = new Float[issueDiscrete.getValues().size()];
            for( @SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
            	prefProbArray[v_n] = (float) (prefProbMatrix[i_n][v_n] / valuesSum[i_n]);
            	worsePrefProbArray[v_n] = (float) (worsePrefProbMatrix[i_n][v_n] / worseValuesSum[i_n]);
                v_n++;
            }
            prefProbMatrix[i_n] = prefProbArray;
            worsePrefProbMatrix[i_n] = worsePrefProbArray;
            i_n++;
        }

        // 4) apply time dependent function
        Float[][] timeBiasedProbMatrix = new Float[issues.size()][];
        
//        float originalValue;
        float normalizedValue;
        Float prefValue;
        Float worsePrefValue;

        int i = 0;
        for(Issue issue : issues) {
        	int j = 0;
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            Float[] probtArray = new Float[issueDiscrete.getValues().size()]; // array of probability * t
            for(@SuppressWarnings("unused") ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                // originalValue = origProbMatrix[i][j];
                normalizedValue = normProbMatrix[i][j];
                prefValue = (Float) prefProbMatrix[i][j];
                worsePrefValue = (Float) worsePrefProbMatrix[i][j];

                // probtArray[j] = normalizedValue;
                // probtArray[j] = (originalValue * time * time) - (normalizedValue * (time * time - 1));
                
                
                // if we are in the last couple of 0.05 time
                if (time < worseStartTime) {
                	probtArray[j] = (float) ((prefValue * Math.pow((time-startnegotime)/(1-startnegotime), timepower) ) - (normalizedValue * (Math.pow((time-startnegotime)/(1-startnegotime), timepower) - 1)));

                } else {
                	probtArray[j] = (float) ((worsePrefValue * Math.pow((time-worseStartTime)/(1-worseStartTime), timepower) ) - (prefValue * (Math.pow((time-worseStartTime)/(1-worseStartTime), timepower) - 1)));

                }
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
        System.out.println("Picked index after roulette: " + Arrays.toString(picked_values_index));

        return newBid;
    }
    
    
    public Float[] occurrence(Value[][] bidmatrix){
    	Value[][] newbidmatrix = new Value[bidmatrix[0].length][bidmatrix.length];// flip bidmatrix
        for (int i = 0; i < bidmatrix.length; i++)
            for (int j = 0; j < bidmatrix[0].length; j++) 
            	newbidmatrix[j][i] = bidmatrix[i][j];
//        System.out.println("newbidmatrix: " + Arrays.deepToString(newbidmatrix));
        
        Long[] valoccur = new Long[issues.size()];
    	for (int j = 0; j < issues.size(); j++){
//	        Value[] issuevalue = new Value[valueNameMatrix[j].length];
	        
//            System.out.println("newbidmatrix[j]: " + Arrays.deepToString(newbidmatrix[j]));
            Map<Object, Long> map = Arrays.stream(newbidmatrix[j])
            	    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            System.out.println("map: " + map); // occurrence of each value

            Long maxValueInMap = Collections.max(map.values());
            for (Entry<Object, Long> entry : map.entrySet()) {  // Iterate through hashmap
                if (entry.getValue()==maxValueInMap) {
                    System.out.println(entry.getKey());     // Print the value with most occurrence
                    System.out.println(entry.getValue());
                    valoccur[j] = entry.getValue();
                }
            }
    	}
        System.out.println("valfreq: " + Arrays.deepToString(valoccur));
        Float[] valfreq = new Float[issues.size()];
        for (int i = 0; i < valoccur.length; i++)
        	valfreq[i] = (float) valoccur[i]/b;
        System.out.println("valfreq: " + Arrays.deepToString(valfreq));
    	return valfreq;
    }
}
