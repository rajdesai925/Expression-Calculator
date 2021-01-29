package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	StringTokenizer str = new StringTokenizer(expr, delims, false);
    	String exp = expr;
    	while (exp.indexOf(' ') != -1) {
    		if (exp.indexOf(' ') == 0) {
    			exp = exp.substring(1);
    		} else {
    			exp = exp.substring(0, exp.indexOf(' ')) + exp.substring(exp.indexOf(' ') + 1);
    		}
    	}
		String s = str.nextToken();
		//System.out.println(str.countTokens());
		boolean add = true;
    	// a+57*ab[cd*e]/a+  (ab[d+t]-r)+ef[45 *   (x+y)]
		while (exp.charAt(0) == '(') {
			exp = exp.substring(1);
		}
    	while (str.hasMoreTokens()) {
    		//System.out.println(s);
    		while (Character.isDigit(s.charAt(0)) && str.hasMoreTokens()) {
    			//System.out.println(s);
    			s = str.nextToken();
    			exp = exp.substring(exp.indexOf(s));
    		}
    		//System.out.println(s + " ->  " + exp);
    		if (exp.length() > s.length()) {
    			exp = exp.substring(s.length());
    		}
    		if (exp.charAt(0) == '[') {
    			Array arr = new Array(s);
    			for (int i = 0; i < arrays.size(); i++) {
    				if (arr.equals(arrays.get(i))) {
    					add = false;
    				}
    			}
    			if (add == true) {
					arrays.add(arr);
				}
    		} else if (!(Character.isDigit(s.charAt(0)))){
    			Variable temp = new Variable(s);
    			for (int k = 0; k < vars.size(); k++) {
    				if (temp.equals(vars.get(k))) {
    					add = false;
    				}
    			}
    			if (add == true) {
	    			vars.add(temp);
				}
    		}
    		if (str.hasMoreTokens()) {
    			s = str.nextToken();
    		} else {
    			break;
    		}
    		exp = exp.substring(exp.indexOf(s));
    		add = true;
    	}
    	
    	

    	if(!(Character.isDigit(exp.charAt(0))) && exp.charAt(0) != ']' && exp.charAt(0) != ')') {
    		//System.out.println("///");
    		for (int m = 0; m < exp.length(); m++) {
    			if (!(Character.isLetter(exp.charAt(m)))) {
    				exp = exp.substring(0, m);
    			}
    		}
    		Variable temp = new Variable(exp);
    		for (int n = 0; n < vars.size(); n++) {
				if (temp.equals(vars.get(n))) {
					add = false;
				}
			}
    		
			if (add == true) {
    			vars.add(temp);
			}
    	}
    	
    	System.out.println(arrays.toString());
    	System.out.println(vars.toString());
    	//System.out.println(exp);

    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	// a              -  (   b  +   A [     B  [      2      +    0       *        a          ]     ]      )    *     d    +       3 =-106
    	
    	String s = expr;
    	
    	while (s.indexOf(' ') != -1) {
    		if (s.indexOf(' ') == 0) {
    			s = s.substring(1);
    		} else {
    			s = s.substring(0, s.indexOf(' ')) + s.substring(s.indexOf(' ') + 1);
    		}
    	}
    
    	System.out.println("=====>  " + s);
    	
    	Stack<Integer> open = new Stack<Integer>();
    	open.push(-1);
    	Stack<Integer> ops = new Stack<Integer>();
    	ops.push(-1);
    	int o = ops.peek();
    	Stack<Integer> close = new Stack<Integer>();
    	close.push(-1);
    	
    	String temp;
    	int tempSol;
    	float solution = 0;
		
    	if (s.indexOf('(') == -1 && s.indexOf('[') == -1) {		//base case
    		return solve(s, vars, arrays);
    	} 
    	
    	for (int i = 0; i < s.length(); i++) {
    		if (s.charAt(i) == '+' || s.charAt(i) == '-' 
    				|| s.charAt(i) == '*' || s.charAt(i) == '/') {
    			ops.push(i);
    			//System.out.println("iiiiiiiii1>   " + s.charAt(i));
    		} else if (s.charAt(i) == '(' || s.charAt(i) == '[') {
    			open.push(i);
    			o = ops.peek();
    			//System.out.println("iiiiiiiii2>   " + s.charAt(i));
    		} else if (s.charAt(i) == ')' || s.charAt(i) == ']') {
    			temp = s.substring(open.peek() + 1, i);
    			//System.out.println("evaluate sends to solve >  " + temp);
    			solution = solve(temp, vars, arrays);
    			if (s.charAt(i) == ')') {					//parentheses
    				temp = Float.toString(solution);
    				if (solution < 0) {
    					temp = '!' + temp.substring(1);
    				}
    				//System.out.println("tttttttt2 >  " + temp);
    				s = s.substring(0, open.peek()) + temp + s.substring(i + 1);
    				//System.out.println("returning1 :  " + s);
    				open.pop();
    				return evaluate(s, vars, arrays);
    			} else if (s.charAt(i) == ']') {			//arrays
    				tempSol = (int) solution;
    				int x = open.pop();
    				//System.out.println("tempsol ====  " + tempSol);
    				//System.out.println("xxxxxxxxxx>   " + x);
    				//System.out.println("open.peek >   " + open.peek());
    				//System.out.println("close.peek >   " + close.peek());
    				//System.out.println("ops.peek >   " + ops.peek() + "      oooooo> " + o);
    				if (o > open.peek() && o > close.peek()) {
    					temp = s.substring(o + 1, x);
    					//System.out.println("ooooooooo>  " + temp);
    					for (int k = 0; k < arrays.size(); k++) {
    						if (arrays.get(k).name.equals(temp)) {
    							if (arrays.get(k).values[tempSol] < 0) {
    								String fix = Integer.toString(arrays.get(k).values[tempSol]);
    								fix = '!' + fix.substring(1);
    								s = s.substring(0, o + 1) + fix + s.substring(i + 1);
    							} else {
    								s = s.substring(0, o + 1) + arrays.get(k).values[tempSol] 
    										+ s.substring(i + 1);
    							}
    						}
    					}
    				} else if (open.peek() > o && open.peek() > close.peek()) {
    					temp = s.substring(open.peek() + 1, x);
    					//System.out.println("ooooopppppp>  " + temp);
    					for (int k = 0; k < arrays.size(); k++) {
    						if (arrays.get(k).name.equals(temp)) {
    							if (arrays.get(k).values[tempSol] < 0) {
    								String fix = Integer.toString(arrays.get(k).values[tempSol]);
    								fix = '!' + fix.substring(1);
    								s = s.substring(0, open.peek() + 1) + fix + s.substring(i + 1);
    							} else {
    								s = s.substring(0, open.peek() + 1) + arrays.get(k).values[tempSol] 
    										+ s.substring(i + 1);
    							}
    						}
    					}
    				} else if (close.peek() > o && close.peek() > open.peek()) {
    					temp = s.substring(close.peek() + 1, x);
    					//System.out.println("ccccccccccc>  " + temp);
    					for (int k = 0; k < arrays.size(); k++) {
    						if (arrays.get(k).name.equals(temp)) {
    							if (arrays.get(k).values[tempSol] < 0) {
    								String fix = Integer.toString(arrays.get(k).values[tempSol]);
    								fix = '!' + fix.substring(1);
    								s = s.substring(0, x + 1) + fix + s.substring(i + 1);
    							} else {
    								s = s.substring(0, x + 1) + arrays.get(k).values[tempSol] 
        									+ s.substring(i + 1);
    							}
    						}
    					}
    				} else if (close.peek() == -1 && open.peek() == -1 && o == -1) {
    					temp = s.substring(0, x); 
    					//System.out.println("-----1111111>  " + temp);
    					for (int k = 0; k < arrays.size(); k++) {
    						if (arrays.get(k).name.equals(temp)) {
    							if (arrays.get(k).values[tempSol] < 0) {
    								String fix = Integer.toString(arrays.get(k).values[tempSol]);
    								fix = '!' + fix.substring(1);
    								s = fix + s.substring(i + 1);
    							} else { 
    								s = arrays.get(k).values[tempSol] + s.substring(i + 1);
    							}
    						}
    					}
    				}
        			close.push(i);
    				//System.out.println("returning2 :  " + s);
    				return evaluate(s, vars, arrays);
    				
    			}
    				
    		}
    	}
    	
 
    	return 0;
    }
    	
    	
    private static float solve (String s, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	Stack<Float> num = new Stack<Float>();
    	Stack<Character> ops = new Stack<Character>();
    	//fills both stacks, evaluates * and /
    	StringTokenizer str = new StringTokenizer(s, delims, true);
    	float num1 = 0;
    	float num2 = 0;
    	char c;
    	while (str.hasMoreTokens()) {
    		String temp = str.nextToken();
    		if (temp.charAt(0) == ' ') {
    			continue;
    		}
    		//System.out.println("0000000>  " + temp);
    		if (Character.isDigit(temp.charAt(0))) {
    			num.push(Float.parseFloat(temp));
    			//System.out.println("nnnnnnn>  " + num.peek());
    		} else if (temp.charAt(0) == '!') {
    			temp = temp.substring(1);
    			num.push(Float.parseFloat(temp) * -1);
    		} else if (Character.isLetter(temp.charAt(0))) {		
    			for (int i = 0; i < vars.size(); i++) {
    					if (temp.equals(vars.get(i).name)) {
    				num.push((float)vars.get(i).value);
    				}
    			}		
    		} else {	
    			c = temp.charAt(0);
    			if (c == '*') {
    				temp = str.nextToken();
    				if (temp.charAt(0) == '!') {
    	    			temp = temp.substring(1);
    	    			num.push(Float.parseFloat(temp) * -1);
    	    		} else if (Character.isDigit(temp.charAt(0))) {
    	    			num.push(Float.parseFloat(temp));
    	    		} else if (Character.isLetter(temp.charAt(0))) {		
    	    			for (int i = 0; i < vars.size(); i++) {
        					if (temp.equals(vars.get(i).name)) {
        						num.push((float)vars.get(i).value);
        					}
    	    			}		
    	    		}
    				//System.out.println("nnnnnn1>  " + num.peek());
    				num2 = num.pop();
    				num1 = num.pop();
    				num.push(num1 * num2);
    			} else if (c == '/') {
    				temp = str.nextToken();
    				if (temp.charAt(0) == '!') {
    	    			temp = temp.substring(1);
    	    			num.push(Float.parseFloat(temp) * -1);
    	    		} else if (Character.isDigit(temp.charAt(0))) {
    	    			num.push(Float.parseFloat(temp));
    	    		} else if (Character.isLetter(temp.charAt(0))) {		
    	    			for (int i = 0; i < vars.size(); i++) {
        					if (temp.equals(vars.get(i).name)) {
        						num.push((float)vars.get(i).value);
        					}
    	    			}		
    	    		}
    				//System.out.println("nnnnnn2>  " + num.peek());
    				num2 = num.pop();
    				num1 = num.pop();
    				num.push(num1 / num2);
    			} else {
    				ops.push(c);
    			}
    		}
    	}
    	
    	//reverses both stacks
    	Stack<Float> numRev = new Stack<Float>();
    	Stack<Character> opsRev = new Stack<Character>();
    	while (num.isEmpty() == false) {
    		float t = num.pop();
    		numRev.push(t);
    	}
    	while (ops.isEmpty() == false) {
    		char cc = ops.pop();
    		opsRev.push(cc);
    	}
    	
    	//evaluates + and -
    	//System.out.println("ppppppppppppp>   " + numRev.peek());
    	while (opsRev.isEmpty() == false) {
    		num2 = numRev.pop();
    		num1 = numRev.pop();
    		c = opsRev.pop();
    		if (c == '+') {
    			numRev.push(num2 + num1);
    		} else {
    			numRev.push(num2 - num1);
    		}
    	}
    	System.out.println("solve returns>   " + numRev.peek());

    	return numRev.peek();
    	
    }
}
