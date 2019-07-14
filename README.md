#Design

Alice and Bob each have a server running

The test class orchestrating to and from amounts from Bob and Alice's trustlines is an 
entity that is trusted in this case.

Basically i am saying test class is the orchestrator/oracle in most networks.

Alice or the payer in any case should be approving the transfer requests

This is done via ECDSA signing of payloads in this case.

Authentication is also built into both the user's server using ECDSA(could be done differently for sure)
The uuid in the header is the uuid of the entity that is running the server.
Authentication is achieved via the orchestrator oracle signing the uuid with the 
orchestrators/oracles private key and with the data of the uuid  and put these in the `signed-uuid` header.

ECDSA is achieved via the `https://github.com/google/tink` library.If you want a curve not covered by this
library using bouncy castle etc, but all NIST curves are covered.
 

The Payer validates/approves a transfer from their wallet via checking that the request comes from the 
orchestrator and returns back a signed payload to the orchestrator, os that the orchestrator can then 
initiate the transfer with the payload provided by Alice(i.e the payer in this case)

Payee i.e Bob does not have restrictions on receiving the money.
Bob does only accept requests from the orchestrator and hence the same authentication.
Also each transfer to Bob carries the approval(ECDSA signature from Alice(payer))


# Getting Started

## step 1
run `up.sh`

this will start Alice and Bob's servers

# step 2
run `transfer.sh`

This will transfer 


Alice Transfer 10 units to bob using TrustLine transfer object

It uses ECDSA to sign the transfer request object meant for the recipient of the object to verify that 
