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
            prof.setParameter(Profile.LOCAL_HOST, "10.33.0.2");
            prof.setParameter(Profile.LOCAL_PORT, "1099");

            AgentContainer ServerContainer = rt.createMainContainer(prof);

            for (int i = 1; i <= 15; i++) {
                String name = "ticket_agent" + i;

                AgentController ticket_agent = ServerContainer.createNewAgent(
                        name,
                        "TicketAgent",
                        null
                );

                ticket_agent.start();
                System.out.println("[" + name + "] запускается");

                Thread.sleep(10);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
