import json
from pade.core.agent import Agent
from pade.acl.messages import ACLMessage
from pade.misc.utility import display_message


class QuestionAgent(Agent):
    def __init__(self, aid, question_data):
        super().__init__(aid=aid)
        self.question = question_data


    def on_start(self):
        super().on_start()
        display_message(self.aid.name, "Запущен")


    def react(self, message):
        if message.performative == ACLMessage.REQUEST:
            reply = message.create_reply()
            reply.set_performative(ACLMessage.INFORM)
            reply.set_sender(self.aid)
            reply.set_content(json.dumps(self.question))
            self.send(reply)

            display_message(self.aid.name, f"Отправил данные: {self.question}")
