# nego_agent
## Negotiation Agent

Building a negotiation agent for the Automated Negotiation Agent Competition (ANAC) using the GENIUS (General Environment for Negotiation with Intelligent Multi-Purpose Usage Simulation) framework.

# Tasks

## Milestones

1) 27 Monday Evening -> Basic Model
2) 29 Wednesday Evening -> Time + Classification
3) 3 Sunday Evening -> Use classification knowledge to adjust weights and do actions
4) 5 Tuesday Morning -> Hand in

# Base model tasks

## Reuben

1) Getting profile variables and storing them into a 2D matrix, string 2D matrix for mapping.
2) Make them into probabilities (using Linear Function).
3) 1st round flag.

## Daniel

1) Create uniform random vector for selecting value of each issue. [COMPLETED]
2) Implement the roulette selection method. [COMPLETED]
3) Put them into a bid using string mapping matrix.
4) Make counter offer, u(bid_offer) <= u(last_offer): accept, otherwise counter-offer(bid_offer).
