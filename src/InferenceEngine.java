import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InferenceEngine {

	List<Knowledge> queryList = new ArrayList<Knowledge>();
	Map<String, List<Knowledge>> knowledgeBase = new HashMap<String, List<Knowledge>>();
	Set<Knowledge> knowledgeBaseSet = new HashSet<Knowledge>();
	final static String fileName = "input0.txt";
	final static String outputFileName = "output.txt";
	public static void main(String[] args) {
		new InferenceEngine().init();
	}

	private void init() {
		processFile(fileName);
		//displayKnowledgeBase();		
		Map<String, List<Knowledge>> clonedKnowledgeBase =null;
		Set<Knowledge> clonedKnowledgeBaseSet = null;
		String key="";
		BufferedWriter bw =null;
		try {			
			bw= new BufferedWriter(new FileWriter(outputFileName));
			for(Knowledge knowledge : queryList) {
				key = knowledge.knowledge.get(0).toString();
				knowledge.knowledge.get(0).predicate = key.charAt(0)=='~'?key.substring(1):"~"+key;
				clonedKnowledgeBase   = deepCloneMap(knowledgeBase);
				updatePredicateToMap(knowledge, clonedKnowledgeBase);
				clonedKnowledgeBaseSet=clonedKnowledgeBase(knowledgeBaseSet);
				clonedKnowledgeBaseSet.add(knowledge);				
				System.out.println("\n \n \n \n");
				if(DFS(knowledge, clonedKnowledgeBase,clonedKnowledgeBaseSet)) 
					bw.write("TRUE");
				else 
					bw.write("FALSE");
				bw.newLine();
				bw.flush();
			}			
			bw.close();		
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean DFS(Knowledge query,Map<String, List<Knowledge>> map, Set<Knowledge> knowledgeSet) {
		if(query.knowledge.size()==0)return true;
		String key = "";
		Knowledge tempKnowledge=null;
		for(Predicate predicate : query.knowledge) {
			key = predicate.toString();
			key = key.charAt(0)=='~'?key.substring(1):"~"+key;
			if(!map.containsKey(key))continue;			
			for(Knowledge knowledge : map.get(key)) {
				System.out.println(query+"  ---> "+knowledge);
				/*if((query+"  ---> "+knowledge).equals("~C(z5,z5)|~C(y5,PHD)|~A(y5,y5)|  ---> A(x6,y6)|~C(y6,x6)|"))
					System.out.println("testBreak");*/
				tempKnowledge = resolveStatement(knowledge,predicate,query);
				if(tempKnowledge==null || knowledgeSet.contains(tempKnowledge)|| checkIfPartOfKnowledgeSet(knowledgeSet, tempKnowledge))continue;
						/*|| tempKnowledge.partofKnowledge(knowledge)
						|| tempKnowledge.partofKnowledge(query))continue;*/
				else if(tempKnowledge.knowledge.size()==0)return true;				
				else {
					knowledgeSet.add(tempKnowledge);
					//updatePredicateToMap(tempKnowledge,map);
				}
				if(DFS(tempKnowledge,map,knowledgeSet))return true;				
			}
		}

		return false;
	}


	private Knowledge resolveStatement(Knowledge knowledge1,Predicate predicate,Knowledge query) {
		// knowledge 1 and knowledge 2 are unified and updated here before resolving
		// knowledge 2 has single predicate
		String key = predicate.toString();
		Map<String,String> unifyMap=null;
		key = key.charAt(0)=='~'?key.substring(1):"~"+key;		
		for(Predicate predicateInKnowledge : knowledge1.knowledge) {
			if(predicateInKnowledge.predicate.equals(key)) {
				unifyMap = unify(predicateInKnowledge.argumentList, predicate.argumentList, new HashMap<String,String>());
				if(unifyMap !=null)break;
			}
		}

		if(unifyMap !=null){
			Knowledge knowledge = updateSentence(knowledge1,unifyMap);
			Knowledge queryKnowledge = updateSentence(query,unifyMap);
			predicate = updatePredicate(predicate.deepClone(),unifyMap);
			//queryKnowledge.deletePredicate(predicate);
			predicate.predicate=predicate.predicate.charAt(0)=='~'?predicate.predicate.substring(1):"~"+predicate.predicate;			
			//knowledge.deletePredicate(predicate);
			predicate.predicate=predicate.predicate.charAt(0)=='~'?predicate.predicate.substring(1):"~"+predicate.predicate;			
			return new Knowledge(knowledge, queryKnowledge);
		}else return null;		
	}

	private Map<String,String> unify(List<String> argumentList1,List<String> argumentList2,Map<String,String> keyValue){		 
		String argument1,argument2;
		for(int i=0; i <argumentList1.size();i++) {
			argument1 = argumentList1.get(i);
			argument2 = argumentList2.get(i);

			if(argument1.equals(argument2))continue;
			else if(Character.isLowerCase(argument1.charAt(0)))keyValue.put(argument1,argument2);
			else if(Character.isLowerCase(argument2.charAt(0)))keyValue.put(argument2,argument1);
			else return null;
		}		
		return keyValue;
	}

	private Knowledge updateSentence( Knowledge knowledge, Map<String,String> map){
		if(map.isEmpty())return new Knowledge(knowledge);
		List<Predicate> updatedKnowledge  = new ArrayList<Predicate>();
		for(Predicate predicate :knowledge.knowledge) {
			updatedKnowledge.add(updatePredicate(predicate.deepClone(),map));
		}
		return new Knowledge(updatedKnowledge);
	}

	private Predicate updatePredicate(Predicate predicate,Map<String,String> map) {
		String argument="";
		for(int i=0;i<predicate.argumentList.size();i++) {
			if(map.containsKey(predicate.argumentList.get(i))) {
				argument=map.get(predicate.argumentList.get(i));					
				predicate.updateVariable(i, argument);
			}
		}
		return predicate;
	}

	private Map<String, List<Knowledge>> deepCloneMap(Map<String, List<Knowledge>> knowledgeBase) {
		Map<String, List<Knowledge>> clonedKnowledgeBase = new HashMap<String, List<Knowledge>>();
		List<Knowledge> list =null;
		for(Map.Entry<String,  List<Knowledge>> entry : knowledgeBase.entrySet()) {
			list = new ArrayList<Knowledge>();
			for(Knowledge knowledge : entry.getValue()) {
				list.add(new Knowledge(knowledge));
			}
			clonedKnowledgeBase.put(entry.getKey(), list);
		}
		return clonedKnowledgeBase;
	}

	private Set<Knowledge> clonedKnowledgeBase(Set<Knowledge> KnowledgeBaseSet){
		Set<Knowledge> clonedKnowledgeBaseSet = new HashSet<Knowledge>();
		Iterator<Knowledge> itr = knowledgeBaseSet.iterator();
		while(itr.hasNext())clonedKnowledgeBaseSet.add(new Knowledge(itr.next()));		
		return clonedKnowledgeBaseSet;
	}

	private void processFile(String fileName) {
		int counter=1;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			//query processing
			int queryCount =  Integer.parseInt(br.readLine()); 
			for(int i=0;i<queryCount;i++)
				queryList.add(new Knowledge(br.readLine().trim(),counter++));

			//knowledge Base 
			int knowledgeBaseCount =  Integer.parseInt(br.readLine()); 
			Knowledge knowledge =null;
			for(int i=0;i<knowledgeBaseCount;i++) {
				knowledge = new Knowledge(br.readLine(),counter++);
				knowledgeBaseSet.add(knowledge);
				updatePredicateToMap(knowledge,knowledgeBase);					
			}		

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePredicateToMap(Knowledge knowledge,Map<String, List<Knowledge>> clonedknowledgeBase) {
		List<Knowledge> sentenceList=null;
		Set<String> set=new HashSet<String>();;
		for(Predicate predicate : knowledge.knowledge) {
			if(!set.contains(predicate.toString())){
				set.add(predicate.toString());
				sentenceList= clonedknowledgeBase.containsKey(predicate.toString())?
						clonedknowledgeBase.get(predicate.toString()):
							new ArrayList<Knowledge>();
						sentenceList.add(knowledge);
						clonedknowledgeBase.put(predicate.toString(), sentenceList);
			}
		}
	}
	
	private boolean checkIfPartOfKnowledgeSet(Set<Knowledge> knowledgeSet, Knowledge knowledge) {
		Iterator<Knowledge> itr = knowledgeSet.iterator();
		while(itr.hasNext()) {
			if(knowledge.partofKnowledge(itr.next()))return true;		
		}
		return false;
	}

	private void displayKnowledgeBase() {
		for (Map.Entry<String, List<Knowledge>> entry : knowledgeBase.entrySet())
		{
			System.out.println();
			System.out.println("Predicate key is " + entry.getKey());
			for( Knowledge knowledge : entry.getValue()){
				for(Predicate p : knowledge.knowledge){
					System.out.print(p.predicate +" ");
					for(String s : p.argumentList){
						System.out.print(s + " ");
					}
				}
				System.out.println();
			}
		}
	}
}
