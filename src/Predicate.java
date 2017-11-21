import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Predicate  implements Comparable<Predicate>{
	String predicate;
	String predicateWithVariable;
	List<String> argumentList;
	private Predicate(String predicate,List<String> argumentList){
		this.predicate=predicate;
		this.argumentList=argumentList;
		this.predicateWithVariable = toString(true);
	}
	Predicate(String predicateString, int index){
		this.predicate= predicateString.substring(0, predicateString.indexOf('('));
		this.argumentList = new ArrayList<String>(
				Arrays.asList(predicateString.substring(predicateString.indexOf('(')+1, predicateString.indexOf(')')).split(",")));
		String argument;
		for(int i =0 ; i < argumentList.size();i++) {
			argument=argumentList.get(i).trim();
			if(argument.length()==1 && Character.isLowerCase(argument.charAt(0)))argument+=index;
			argumentList.remove(i);
			argumentList.add(i,argument);						
		}
		this.predicateWithVariable = toString(true);
	}

	public Predicate deepClone() {
		List<String> argumentList =  new ArrayList<String>();
		for(String argument : this.argumentList)argumentList.add(argument);
		return new Predicate(toString(),argumentList);	
	}

	public String toString() {
		return this.predicate;
	}
	public String toString(boolean usedInComparable) {
		String string = this.predicate.toString()+"(";
		boolean flag =true;
		for(String argument : this.argumentList) {
			//if(Character.isLowerCase(argument.charAt(0)))argument=argument.charAt(0)+"";
			if(flag)string+=argument;
			else string+=","+argument;
			flag=false;
		}
		return string+")";
	}
	@Override
	public int hashCode() {		
		return Objects.hashCode(this.toString(true));
	}
	@Override
	public boolean equals(Object obj) {
		return toString(true).equals(((Predicate)obj).toString(true));
	}
	public int compareTo(Predicate  predicate){ 
		return this.toString(true).compareTo(predicate.toString(true));
	}

	public void updateVariable(int index, String variable) {
		this.argumentList.remove(index);
		this.argumentList.add(index,variable);
		this.predicateWithVariable = toString(true);
	}
}