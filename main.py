import json
from pade.acl.aid import AID
from pade.misc.utility import start_loop

from coordinator_agent import CoordinatorAgent
from question_agent import QuestionAgent
from ticket_agent import TicketAgent


def main():
    agents = []
    with open('questions.json', 'r', encoding='utf-8') as q:
        questions = json.load(q)

    port = 8001
    question_agents_aids = []

    for q in questions:
        aid = AID(name=f"question_{q['id']}localhost:{port}")
        question_agents_aids.append(aid)

        agent = QuestionAgent(aid, q)
        agents.append(agent)

        port += 1

    ticket_aid = AID(name="ticket@localhost:8100")
    ticket_agent = TicketAgent(ticket_aid)
    agents.append(ticket_agent)

    coordinator_aid = AID(name="coordinator@localhost:8200")
    coordinator_agent = CoordinatorAgent(
        coordinator_aid,
        question_agents=question_agents_aids,
        ticket_agent=ticket_aid
    )
    agents.append(coordinator_agent)

    start_loop(agents)


if __name__ == "__main__":
    main()
