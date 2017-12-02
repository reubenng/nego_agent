import java.util.Arrays;

public class test_feature {
        public static void main(String[] args) {

            // Inputs (double[][] normProbMatrix, double[] agent1FreqMatrix, double[] agent2FreqMatrix, double time)
            int lastOfferAgent = 2;
            
            double[][] normProbMatrix = new double[][]{
                { 0.1, 0.3, 0.6 },
                { 0.2, 0.3, 0.5 },
                { 0.25, 0.30, 0.45 },
                { 0.05, 0.3, 0.65 }
            };

            int NumberOfIssues = normProbMatrix.length;
            int NumberOfValues = normProbMatrix[0].length;

            double[] agent1FreqMatrix = { 0.3, 0.4, 0.2, 0.8 };
            double[] agent2FreqMatrix = { 0.5, 0.2, 0.6, 0.1 };

            double[] freqMatrix = new double[NumberOfIssues];

            // prefProbMatrix variable declaration and init
            double[][] prefProbMatrix = new double[NumberOfIssues][NumberOfValues];
            double oldValue;
            double exponent;
            double i_freq;

            double[] valuesSum = new double[NumberOfIssues];

            // if last offer is from Agent 1 
            if (lastOfferAgent == 1) {
                freqMatrix = agent2FreqMatrix;
            
            // if last offer is from Agent 2
            } else {
                freqMatrix = agent1FreqMatrix;
            }

            // create a bid that is closer to what Agent 2 wants
            for(int i_n = 0 ; i_n < NumberOfIssues; i_n++) {

                i_freq = freqMatrix[i_n];

                // change every value prob accordingly
                for(int v_n = 0; v_n < NumberOfValues; v_n++) {
                    oldValue = normProbMatrix[i_n][v_n];
                    exponent = 0.5 + (1 - i_freq);
                    prefProbMatrix[i_n][v_n] =  Math.pow(oldValue, exponent);

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

            System.out.println("NormProbArray" + Arrays.deepToString(normProbMatrix));
            System.out.println("PrefProbArray" + Arrays.deepToString(prefProbMatrix));
        }
    
    }
    