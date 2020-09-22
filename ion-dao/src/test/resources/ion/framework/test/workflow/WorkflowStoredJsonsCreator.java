package ion.framework.test.workflow;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ion.core.ConditionType;
import ion.core.DACPermission;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredPermissions;
import ion.framework.meta.plain.StoredPropertyPermissions;
import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.framework.workflow.plain.StoredWorkflowSelectionProvider;
import ion.framework.workflow.plain.StoredWorkflowState;
import ion.framework.workflow.plain.StoredWorkflowTransition;

import com.google.gson.Gson;

public class WorkflowStoredJsonsCreator {

	public static void main(String[] args) {
		Gson g = new Gson();

		List<StoredWorkflowState> mammalStates = new ArrayList<StoredWorkflowState>();
			
				List<StoredCondition> newbieConditions = new ArrayList<StoredCondition>();
				newbieConditions.add(new StoredCondition("age", ConditionType.LESS.getValue(), "2"));
				
				List<StoredPermissions> newbieIitemPermissions = new ArrayList<StoredPermissions>();
				newbieIitemPermissions.add(new StoredPermissions("user", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> newbiePropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ip2 = new ArrayList<StoredPermissions>();
				ip2.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				newbiePropertyPermissions.add(new StoredPropertyPermissions("age", ip2));

				List<StoredWorkflowSelectionProvider> newbieSelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				newbieSelectionProviders.add(new StoredWorkflowSelectionProvider("user", "age"));
				
			StoredWorkflowState newbieState = new StoredWorkflowState("Newbie", newbieConditions, newbieIitemPermissions, newbiePropertyPermissions, newbieSelectionProviders);
			
				List<StoredCondition> denizenConditions = new ArrayList<StoredCondition>();
				denizenConditions.add(new StoredCondition("age", ConditionType.LESS_OR_EQUAL.getValue(), "10"));
				denizenConditions.add(new StoredCondition("age", ConditionType.MORE_OR_EQUAL.getValue(), "2"));
				
				List<StoredPermissions> denizenIitemPermissions = new ArrayList<StoredPermissions>();
				denizenIitemPermissions.add(new StoredPermissions("user", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> denizenPropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ip3 = new ArrayList<StoredPermissions>();
				ip3.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				denizenPropertyPermissions.add(new StoredPropertyPermissions("age", ip3));
	
				List<StoredWorkflowSelectionProvider> denizenSelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				denizenSelectionProviders.add(new StoredWorkflowSelectionProvider("user", "age"));
				
			StoredWorkflowState denizenState = new StoredWorkflowState("Denizen", denizenConditions, denizenIitemPermissions, denizenPropertyPermissions, denizenSelectionProviders);
			
				List<StoredCondition> pensionerConditions = new ArrayList<StoredCondition>();
				pensionerConditions.add(new StoredCondition("age", ConditionType.MORE.getValue(), "10"));
				
				List<StoredPermissions> pensionerIitemPermissions = new ArrayList<StoredPermissions>();
				pensionerIitemPermissions.add(new StoredPermissions("user", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> pensionerPropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ip4 = new ArrayList<StoredPermissions>();
				ip4.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				pensionerPropertyPermissions.add(new StoredPropertyPermissions("age", ip4));
	
				List<StoredWorkflowSelectionProvider> pensionerSelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				pensionerSelectionProviders.add(new StoredWorkflowSelectionProvider("user", "age"));
				
			StoredWorkflowState pensionerState = new StoredWorkflowState("Denizen", pensionerConditions, pensionerIitemPermissions, pensionerPropertyPermissions, pensionerSelectionProviders);

			mammalStates.add(newbieState);
			mammalStates.add(denizenState);
			mammalStates.add(pensionerState);
		
		List<StoredWorkflowTransition> mammalTransitions = new ArrayList<StoredWorkflowTransition>();
		
				String[] roles = new String[2];
				roles[0] = "user";
				roles[0] = "admin";
				
				List<StoredKeyValue> assignments = new ArrayList<StoredKeyValue>();
				
				List<StoredCondition> conditions = new ArrayList<StoredCondition>();
				conditions.add(new StoredCondition("age", ConditionType.LESS.getValue(), "2"));
				
			StoredWorkflowTransition becomeDenizenTransition = new StoredWorkflowTransition("BecomeDenizen", "BecomeDenizen", "MammalsWF","Newbie", "Denizen", false, roles, assignments, conditions);

				String[] pensionerRoles = new String[2];
				roles[0] = "user";
				roles[0] = "admin";
				
				List<StoredKeyValue> pensionerAssignments = new ArrayList<StoredKeyValue>();
				
				List<StoredCondition> pensionerTConditions = new ArrayList<StoredCondition>();
				pensionerTConditions.add(new StoredCondition("age", ConditionType.LESS_OR_EQUAL.getValue(), "10"));
				pensionerTConditions.add(new StoredCondition("age", ConditionType.MORE_OR_EQUAL.getValue(), "2"));
				
			StoredWorkflowTransition becomePensionerTransition = new StoredWorkflowTransition("BecomePensioner", "BecomePensioner", "MammalsWF", "Denizen", "Pensioner", false, pensionerRoles, pensionerAssignments, pensionerTConditions);
			
			mammalTransitions.add(becomeDenizenTransition);
			mammalTransitions.add(becomePensionerTransition);
			
		StoredWorkflowModel MammalsWorkflow = new StoredWorkflowModel("MammalsWF", "Mammals", "Newbie",mammalStates, mammalTransitions);
	
		String mammalsWorkflow = g.toJson(MammalsWorkflow);
		try {
	    FileWriter writer = new FileWriter("mammals.json");
	    writer.write(mammalsWorkflow);  
	    writer.close();  
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } 
		
		List<StoredWorkflowState> dogStates = new ArrayList<StoredWorkflowState>();
			
				List<StoredCondition> puppyConditions = new ArrayList<StoredCondition>();
				puppyConditions.add(new StoredCondition("heigth", ConditionType.LESS.getValue(), "15"));
				
				List<StoredPermissions> puppyItemPermissions = new ArrayList<StoredPermissions>();
				puppyItemPermissions.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				puppyItemPermissions.add(new StoredPermissions("admin", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> puppyPropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ps = new ArrayList<StoredPermissions>();
				ps.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				puppyPropertyPermissions.add(new StoredPropertyPermissions("height", ps));
				
				List<StoredWorkflowSelectionProvider> puppySelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				puppySelectionProviders.add(new StoredWorkflowSelectionProvider("admin", "height"));
				
			StoredWorkflowState puppyState = new StoredWorkflowState("Puppy", puppyConditions, puppyItemPermissions, puppyPropertyPermissions, puppySelectionProviders);

				List<StoredCondition> assistantConditions = new ArrayList<StoredCondition>();
				assistantConditions.add(new StoredCondition("heigth", ConditionType.MORE_OR_EQUAL.getValue(), "15"));
				assistantConditions.add(new StoredCondition("heigth", ConditionType.LESS_OR_EQUAL.getValue(), "50"));
				
				List<StoredPermissions> assistantItemPermissions = new ArrayList<StoredPermissions>();
				assistantItemPermissions.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				assistantItemPermissions.add(new StoredPermissions("admin", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> assistantPropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ps1 = new ArrayList<StoredPermissions>();
				ps1.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				assistantPropertyPermissions.add(new StoredPropertyPermissions("height", ps1));
				
				List<StoredWorkflowSelectionProvider> assistantSelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				assistantSelectionProviders.add(new StoredWorkflowSelectionProvider("admin", "height"));
				
			StoredWorkflowState assistantState = new StoredWorkflowState("Assistant", assistantConditions, assistantItemPermissions, assistantPropertyPermissions, assistantSelectionProviders);
	
				List<StoredCondition> warderConditions = new ArrayList<StoredCondition>();
				warderConditions.add(new StoredCondition("heigth", ConditionType.MORE.getValue(), "50"));
				
				List<StoredPermissions> warderItemPermissions = new ArrayList<StoredPermissions>();
				warderItemPermissions.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				warderItemPermissions.add(new StoredPermissions("admin", DACPermission.WRITE.getValue()));
				
				List<StoredPropertyPermissions> warderPropertyPermissions = new ArrayList<StoredPropertyPermissions>();
				List<StoredPermissions> ps2 = new ArrayList<StoredPermissions>();
				ps2.add(new StoredPermissions("user", DACPermission.READ.getValue()));
				warderPropertyPermissions.add(new StoredPropertyPermissions("height", ps2));
				
				List<StoredWorkflowSelectionProvider> warderSelectionProviders = new ArrayList<StoredWorkflowSelectionProvider>();
				warderSelectionProviders.add(new StoredWorkflowSelectionProvider("admin", "height"));
				
			StoredWorkflowState warderState = new StoredWorkflowState("Warder", warderConditions, warderItemPermissions, warderPropertyPermissions, warderSelectionProviders);
			
			dogStates.add(puppyState);
			dogStates.add(assistantState);
			dogStates.add(warderState);
			
		List<StoredWorkflowTransition> dogTransitions = new ArrayList<StoredWorkflowTransition>();
		
			String[] toAssistantRoles = new String[2];
			roles[0] = "user";
			roles[0] = "admin";
			
			List<StoredKeyValue> toAssistantAssignments = new ArrayList<StoredKeyValue>();
			
			List<StoredCondition> toAssistantConditions = new ArrayList<StoredCondition>();
			toAssistantConditions.add(new StoredCondition("age", ConditionType.LESS.getValue(), "2"));
		
		StoredWorkflowTransition toAssistant = new StoredWorkflowTransition("ToAssistant", "ToAssistant", "DogsWF", "Puppy", "Assistant", false, toAssistantRoles, toAssistantAssignments, toAssistantConditions);
		StoredWorkflowTransition toWarder = new StoredWorkflowTransition("ToWarder", "ToWarder", "DogsWF", "Puppy", "Warder", false, toAssistantRoles, toAssistantAssignments, toAssistantConditions);
		
		dogTransitions.add(toAssistant);
		dogTransitions.add(toWarder);
		
		StoredWorkflowModel DogsWorkflow = new StoredWorkflowModel("DogsWF", "Dogs", "Puppy",dogStates, dogTransitions);
		
		String dogsWorkflow = g.toJson(DogsWorkflow);
		try {
	    FileWriter writer = new FileWriter("dogs.json");
	    writer.write(dogsWorkflow);  
	    writer.close();  
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } 
	}

}
