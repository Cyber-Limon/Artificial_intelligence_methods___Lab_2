from pade.core.agent import Agent
from pade.acl.messages import ACLMessage
from pade.misc.utility import display_message


class TicketAgent(Agent):
    def __init__(self, aid):
        super().__init__(aid=aid)
        self.questions = []


    def on_start(self):
        super().on_start()
        display_message(self.aid.name, "Запущен")


    def react(self, message):
        if message.performative == ACLMessage.INFORM:
            sender = message.sender
            content = message.content

            display_message(self.aid.name,f"Получено сообщение от {sender.name}: {content}")

            self.questions.append(content)

            display_message(self.aid.name,f"Всего получено сообщений: {len(self.questions)}")
