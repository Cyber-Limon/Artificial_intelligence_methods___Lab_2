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
            prof.setParameter(Profile.LOCAL_HOST, "0.0.0.0");

            AgentContainer ServerContainer = rt.createMainContainer(prof);


            String[] questionAgents = {
                    "q1", "q2", "q3"      // << Имена агентов на клиентской машине
            };


            // Запуск ticket_agent //

            AgentController ticket_agent = ServerContainer.createNewAgent(
                    "ticket_agent",
                    "TicketAgent",
                    null
            );

            ticket_agent.start();
            System.out.println("[ticket_agent] запускается");



            // Запуск coordinator_agent //

            AgentController coordinator_agent = ServerContainer.createNewAgent(
                    "coordinator_agent",
                    "CoordinatorAgent",
                    question_agents
            );

            coordinator_agent.start();
            System.out.println("[coordinator_agent] запускается");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
