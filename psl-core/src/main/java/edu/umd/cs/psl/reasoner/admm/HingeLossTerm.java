/*
 * This file is part of the PSL software.
 * Copyright 2011 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.reasoner.admm;

class HingeLossTerm extends HyperplaneTerm {
	
	private final double weight;
	
	HingeLossTerm(ADMMReasoner reasoner, int[] zIndices, double[] lowerBounds,
			double[] upperBounds, double[] coeffs, double constant, double weight) {
		super(reasoner, zIndices, lowerBounds, upperBounds, coeffs, constant);
		this.weight = weight;
	}
	
	@Override
	protected void minimize() {
		/* Initializes scratch data */
		double a[] = new double[x.length];
		double total = 0.0;
		
		/*
		 * Minimizes without the linear loss, i.e., solves
		 * argmin stepSize/2 * \|x - z + y / stepSize \|_2^2
		 * such that x is within its box
		 */
		for (int i = 0; i < a.length; i++) {
			a[i] = reasoner.z.get(zIndices[i]) - y[i] / reasoner.stepSize;
			
			if (a[i] < lb[i])
				a[i] = lb[i];
			else if (a[i] > ub[i])
				a[i] = ub[i];
			
			total += coeffs[i] * a[i];
		}
		
		/* If the linear loss is NOT active at the computed point, it is the solution... */
		if (total <= constant) {
			for (int i = 0; i < x.length; i++)
				x[i] = a[i];
			return;
		}
		
		/*
		 * Else, minimizes with the linear loss, i.e., solves
		 * argmin weight * coeffs^T * x + stepSize/2 * \|x - z + y / stepSize \|_2^2
		 * such that x is within its box 
		 */
		for (int i = 0; i < a.length; i++) {
			a[i] = reasoner.z.get(zIndices[i]) - y[i] / reasoner.stepSize;
			a[i] -= weight * coeffs[i] / reasoner.stepSize;
			
			if (a[i] < lb[i])
				a[i] = lb[i];
			else if (a[i] > ub[i])
				a[i] = ub[i];
			
			total += coeffs[i] * a[i];
		}
		
		/* If the linear loss IS active at the computed point, it is the solution... */
		if (total >= constant) {
			for (int i = 0; i < x.length; i++)
				x[i] = a[i];
			return;
		}
		
		/* Else, the solution is on the hyperplane */
		solveKnapsackProblem();
	}
}
