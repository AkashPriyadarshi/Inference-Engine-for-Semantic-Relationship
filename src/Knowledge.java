import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Knowledge {
	List<Predicate> knowledge = new ArrayList<Predicate>();


	public Knowledge(Knowledge knowledge) {
		for(Predicate predicate : knowledge.knowledge)this.knowledge.add(predicate.deepClone());
	}

	public Knowledge(Knowledge knowledge1, Knowledge knowledge2) {
		Set<Predicate> set = new HashSet<Predicate>();
		for(Predicate predicate : knowledge1.knowledge)set.add(predicate);
		for(Predicate predicate : knowledge2.knowledge)set.add(predicate);
		Iterator<Predicate> iterator = set.iterator();
		Predicate predicate =null;
		while(iterator.hasNext()) {
			predicate = iterator.next().deepClone();
			predicate.predicate=predicate.predicate.charAt(0)=='~'?predicate.predicate.substring(1):"~"+predicate.predicate;			
			if(!set.contains(predicate)) {
				predicate.predicate=predicate.predicate.charAt(0)=='~'?predicate.predicate.substring(1):"~"+predicate.predicate;
				this.knowledge.add(predicate.deepClone());
			}
		}		
	}

	public Knowledge(String sentenceString,int index) {
		this.knowledge = getSentence(sentenceString, index);	
		Collections.sort(knowledge);
	}

	public Knowledge(List<Predicate> knowledge) {
		this.knowledge = knowledge;
		Collections.sort(knowledge);
	}	

	public boolean isEqual(Knowledge sentence2) {
		return this.toString().equals(sentence2.toString());
	}

	public int size() {
		return this.knowledge.size();
	}

	private List<Predicate> getSentence(String sentenceString, int index) {
		List<Predicate> knowledge = new ArrayList<Predicate>();
		for(String predicate : sentenceString.split("\\|"))
			knowledge.add(new Predicate(predicate.trim(), index));
		return knowledge;
	}

	public String toString() {
		String stringRepresentation="";
		for(Predicate predicate : this.knowledge)stringRepresentation+=predicate.toString(true)+"|";
		return stringRepresentation;
	}



	@Override
	public int hashCode() {
		return Objects.hashCode(this.toString());
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(((Knowledge)obj).toString());
	}

	public void deletePredicate(Predicate predicate) {
		ListIterator<Predicate> itr = this.knowledge.listIterator();
		while(itr.hasNext()) {
			if(itr.next().compareTo(predicate)==0)itr.remove();
		}
	}

	public boolean partofKnowledge(Knowledge knowledgeObj) {
		Set<Predicate> set = new HashSet<Predicate>(this.knowledge);
		for(Predicate predicate : knowledgeObj.knowledge)
			if(!set.contains(predicate))return false;
		return true;		
	}
}
