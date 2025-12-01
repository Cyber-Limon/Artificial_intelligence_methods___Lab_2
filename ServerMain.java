import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;



public class ServerMain {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile prof = new ProfileImpl();
            prof.setParameter(Profile.GUI, "true");
            prof.setParameter(Profile.MAIN, "true");
            prof.setParameter(Profile.LOCAL_HOST, "10.244.215.194");
            prof.setParameter(Profile.LOCAL_PORT, "1099");

            AgentContainer ServerContainer = rt.createMainContainer(prof);

            AgentController coordinator_agent = ServerContainer.createNewAgent(
                    "coordinator_agent",
                    "CoordinatorAgent",
                    null
            );

            coordinator_agent.start();
            System.out.println("[coordinator_agent] запускается");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
