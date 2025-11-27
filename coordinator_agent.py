import json
from pade.core.agent import Agent
from pade.acl.messages import ACLMessage
from pade.misc.utility import display_message


class CoordinatorAgent(Agent):
    def __init__(self, aid, question_agents, ticket_agent):
        super().__init__(aid=aid)

        self.question_agents = question_agents
        self.ticket_agent = ticket_agent
        self.collected_data = {}


    def on_start(self):
        super().on_start()

        display_message(self.aid.name, "Запущен; запрашиваю данные у QuestionAgent")

        for q_aid in self.question_agents:
            msg = ACLMessage(ACLMessage.REQUEST)
            msg.set_sender(self.aid)
            msg.add_receiver(q_aid)
            msg.set_content("SEND_DATA")

            self.send(msg)
            display_message(self.aid.name,f"REQUEST к {q_aid.name}")


    def react(self, message):
        if message.performative == ACLMessage.INFORM:
            sender = message.sender.name
            content = json.loads(message.content)

            display_message(self.aid.name,f"INFORM от {sender}: {content}")

            self.collected_data[sender] = content

            if len(self.collected_data) == len(self.question_agents):
                display_message(self.aid.name,"Все ответы получены. Пересылаю [TicketAgent]")

                msg = ACLMessage(ACLMessage.INFORM)
                msg.add_receiver(self.ticket_agent)
                msg.sender = self.aid
                msg.content = json.dumps(self.collected_data)

                self.send(msg)
