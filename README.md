#Design


Alice and Bob each have a spring boot server running.

The code is identical(shared via the common-services module) except that they have different application
properties.

In real life these should be a common docker image, with unique properties injected in via some
mechanism (probably consul or some other mechanism)

## Security
This implementation assumes a permissioned network and distribute a ECDSA keypair to all network partipants

Security is implemented via Spring security and an `AuthenticationManager` which uses ECDSA to verify that 
the request is signed with a payload == user id of the entity running the server.

This mechanism is similar to jwt(ECDSA verifier) which is probably more standardized and probably a better choise.

ECDSA is implemented via google tink `https://github.com/google/tink` which was one of the reasons for implementing
it the way i have, since tink library is tested to be cross platform and battle tested by google.

The uuid in the header is the uuid of the entity that is running the server for things like apiGateway to 
redirect requests if necessary(not happening here)
## Network Components

* bob's server
* alice's server
* orchestrator (initiates payment requests from Alice to bob in this use case)


## Client

This repo provides a client for all the above network components to interact with each other

## Payment process.
This deviates a little from the original problem describes in that i bring in approval step for mainly proving that 
the payer has actually authorized the payment in case of dispute resolution etc.
The approval step as separate step can probably be skipped, but
* may help with retries by a central orchestrator for failed request because of network partitions etc.
* audits
* transfer reports on the network
without involving the payer directly.


Also may help with things like reserve enforcement on the payers trustline, multiparty contracts etc..
however this step should be opaque to the user if wrapped in a wrapper method if necessary.

A trustline payment request looks like:

``` TrustlineTransfer(
          val transferId: UUID, //idempotent check
          val toEntityUUID: UUID,
          val toEntityName: String,
          val fromEntityUUID: UUID,
          val fromEntityName: String,
          val authcode: String,
          val amount: BigDecimal
  ```

This is sent to the approval of the payer, the payer looks at the `authcode` as an example and 
decides if they should pay the transfer..Could be lot more business rules etc

The payer if he agrees uses ECDSA to sign the `TrustlineTransfer` json representation and sends it back in the 
signature field of the TrustlineTransferResponse class
```
data class TrustlineTransferResponse(
        val transferId: UUID, //idempotent check
        val toEntityUUID: UUID,
        val toEntityName: String,
        val fromEntityUUID: UUID,
        val fromEntityName: String,
        val amount: BigDecimal,
        val authcode:String,
        val signatureOfApproval: String //base 64 encode ECDSA signature..
)
```
                                  
This `signatureOfApproval` is used for any dispute resolution, since it is a cryptographic proof the payer
(here Alice) has agreed to the payment.

After approval transfer is requested from Alice, who verifies transactionId is not already used
(**idempotency check**)

Also verifies signature is what it should be by signing and then comparing the payload sent to it.
If both matches up, it deducts the balance from its trustline balance and attempts to send the same to 
payee(Bob) via the fiegn test client.

Bob's server adds the balance and responds with the UUID of the transaction.

Transaction is stored on bob's side in case request is replayed(idempotency check.. since we assume bob is
non-byzantine)

Also payee server should store the signature, in case payer disputes the transaction with network admin etc
(Not happening in this demo.. but should goto a datastore..)

Also not implemented are **retries** in case of **network latencies** and **partitions**

Also payer Alice's transaction is not reversed as it stands now, but should be reversed if verified that that
the payee(Bob) has not received the payment (would be easy in non byzantine cases much harder in byzantine ones)

The approval step with response signature may help with retries, since approval signature should only have
been generated once.



# Getting Started

## step 1
run `up.sh`

this will start Alice and Bob's servers

# step 2
run `transfer.sh`

This will transfer 


Alice Transfer 10 units to bob using TrustLine transfer object

It uses ECDSA to sign the transfer request object meant for the recipient of the object to verify that 
