package bartMachine;

import java.io.Serializable;

import gnu.trove.list.array.TIntArrayList;

/**
 * This portion of the code that performs the posterior sampling
 * in the Gibbs sampler except for the Metropolis-Hastings tree search step
 * 
 * @author Adam Kapelner and Justin Bleich
 */
public abstract class bartMachine_f_gibbs_internal extends bartMachine_e_gibbs_base implements Serializable{

	/**
	 * Assigns a value to this terminal node; the value is the prediction at this corner of X space.
	 * 
	 * @param node		The node to assign a prediction guess for
	 * @param sigsq		The current guess of the variance of the model errors
	 */
	protected void assignLeafValsBySamplingFromPosteriorMeanAndUpdateYhats(bartMachineTreeNode node, double k) {
		if (node.isLeaf){
				/* //update ypred
				double posterior_var = calcLeafPosteriorVar(node, 1);
				//draw from posterior distribution
				double posterior_mean = calcLeafPosteriorMean(node, 1, posterior_var);
				node.log_lambda_comp_pred = StatToolbox.sample_from_norm_dist(posterior_mean, posterior_var);
				if (node.log_lambda_comp_pred == StatToolbox.ILLEGAL_FLAG){				
					node.log_lambda_comp_pred = 0.0; //this could happen on an empty node
					System.err.println("ERROR assignLeafFINAL " + node.log_lambda_comp_pred + " (sigsq = " + 1 + ")");
				}
				//now update yhats
				node.updateYHatsWithPrediction(); */
			
			//need to update mean, var, a, and b
			//double posterior_var = calcLeafPosteriorVar(node, 1);
			//double posterior_mean = calcLeafPosteriorMean(node, 1, posterior_var);
			double posterior_a = calcLeafPosteriorA(node);
			double posterior_b = calcLeafPosteriorB(node, k);
			
			//sample lambda from invgamma posterior
			node.log_lambda_comp_pred = StatToolbox.sample_from_inv_gamma(posterior_a, posterior_b);
			System.out.println("posterior_a = " + posterior_a + " posterior_b = " + posterior_b + " k = " + k + " log_lambda_comp_pred = " + node.log_lambda_comp_pred);
			//node.log_lambda_comp_pred = StatToolbox.sample_from_trunc_norm_dist(posterior_mean, posterior_var, posterior_a, posterior_b);
			node.updateYHatsWithPrediction();
			
		}
		else {
			assignLeafValsBySamplingFromPosteriorMeanAndUpdateYhats(node.left, k);
			assignLeafValsBySamplingFromPosteriorMeanAndUpdateYhats(node.right, k);
		}
	}

	/**
	 * Calculate the posterior mean of the prediction distribution at a certain node
	 * 
	 * @param node				The node we are calculating the posterior mean for
	 * @param sigsq				The current guess of the variance of the model errors
	 * @param posterior_var		The posterior variance of the prediction distribution at this node
	 * @return					The posterior mean for this node
	 */
	protected double calcLeafPosteriorMean(bartMachineTreeNode node, double sigsq, double posterior_var) {
		return (1 / 1 + node.n_eta / sigsq * node.avgResponse()) * posterior_var;
	}
	
	/**
	 * Calculate the posterior parameter a of the prediction distribution at a certain node
	 * 
	 * @param node				The node we are calculating the posterior a parameter for
	 * @return					The posterior parameter a for this node
	 */
	protected double calcLeafPosteriorA(bartMachineTreeNode node) {
		return node.n_eta + hyper_a - 1;
	}
	
	/**
	 * Calculate the posterior parameter b of the prediction distribution at a certain node
	 * 
	 * @param node				The node we are calculating the posterior b parameter for
	 * @return					The posterior parameter b for this node
	 */
	protected double calcLeafPosteriorB(bartMachineTreeNode node, double k) {
		//should return sum y_i^k + b
		return node.sumResponses_to_the_k(k) + hyper_b;
	}

	

	/**
	 * Calculate the posterior variance of the prediction distribution at a certain node
	 * 
	 * @param node		The node we are calculating the posterior variance for
	 * @param sigsq		The current guess of the variance of the model errors
	 * @return			The posterior variance for this node
	 */
//ES(Update the sigsq_mu)	
	protected double calcLeafPosteriorVar(bartMachineTreeNode node, double sigsq) {
		return 1 / (1 / 1 + node.n_eta / sigsq);
	}
	
//	/**
//	 * Draws one k from the posterior distribution
//	 * 
//	 * @param sample_num	The current sample number of the Gibbs sampler
//	 * @param es			The vector of residuals at this point in the Gibbs chain
//	 */
//
//	protected double drawKFromPosterior(int sample_num, double[] es) {
//		
//		//first calculate the SSE
//		double sse = 0;
//		for (double e : es){
//			sse += e * e; 
//		}
////ES(Look here; update)		
//		//we're sampling from sigsq ~ InvGamma((nu + n) / 2, 1/2 * (sum_i error^2_i + lambda * nu))
//		//which is equivalent to sampling (1 / sigsq) ~ Gamma((nu + n) / 2, 2 / (sum_i error^2_i + lambda * nu))
//		return StatToolbox.sample_from_inv_gamma((1 + es.length) / 2, 2 / (sse + 1 * 1));
//	}
	
	/**
	 * Draws one k from the posterior distribution
	 * 
	 * right now rubbish just to connect everything
	 * 
	 * @param sample_num	The current sample number of the Gibbs sampler
	 * @param es			The vector of residuals at this point in the Gibbs chain
	 */

	protected double drawKFromPosterior(int sample_num, double[] es) {
		
		return Math.random() * 5;
	}

	/**
	 * Pick a random predictor from the set of valid, possible predictors at this point in the tree
	 * 
	 * @param node	The node to pick a predictor for
	 * @return		The index of the picked predictor
	 */
	public int pickRandomPredictorThatCanBeAssigned(bartMachineTreeNode node){
        TIntArrayList predictors = node.predictorsThatCouldBeUsedToSplitAtNode();
        return predictors.get((int)Math.floor(StatToolbox.rand() * pAdj(node)));
	}
	
	/**
	 * Gets the total number of predictors that could be used for rules at this point in the tree
	 * 
	 * @param node 	The node to calculate the number of predictors for
	 * @return		The number of valid predictors
	 */
	public double pAdj(bartMachineTreeNode node){
		if (node.padj == null){
			node.padj = node.predictorsThatCouldBeUsedToSplitAtNode().size();
		}
		return node.padj;
	}	
}
