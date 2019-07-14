package com.amitra.trustlines.model

import java.math.BigDecimal
import java.util.UUID

data class TrustlineTransfer(
        val transferId: UUID, //idempotent check
        val toEntityUUID: UUID,
        val toEntityName: String,
        val fromEntityUUID: UUID,
        val fromEntityName: String,
        val amount: BigDecimal
) {
    fun toTrustLineResponse(signatureOfApproval: String): TrustlineTransferResponse {
        return TrustlineTransferResponse(transferId = this.transferId, toEntityUUID = this.toEntityUUID,
                fromEntityUUID = this.fromEntityUUID,
                toEntityName = this.toEntityName,
                fromEntityName = this.fromEntityName,
                amount = this.amount,
                signatureOfApproval = signatureOfApproval
        )
    }
}

data class TrustlineTransferResponse(
        val transferId: UUID, //idempotent check
        val toEntityUUID: UUID,
        val toEntityName: String,
        val fromEntityUUID: UUID,
        val fromEntityName: String,
        val amount: BigDecimal,
        val signatureOfApproval: String //base 64 encode ECDSA signature..
) {
    fun toTrustLineWithSig(port:Int): TrustlineTransferWithSig {
        return TrustlineTransferWithSig(transferId = this.transferId, toEntityUUID = this.toEntityUUID,
                fromEntityUUID = this.fromEntityUUID,
                toEntityName = this.toEntityName,
                fromEntityName = this.fromEntityName,
                amount = this.amount,
                signatureOfApproval = this.signatureOfApproval,
                toEntityPort = port
        )
    }
}

//just a better name
data class TrustlineTransferWithSig(
        val transferId: UUID, //idempotent check
        val toEntityUUID: UUID,
        val toEntityName: String,
        val toEntityPort: Int,
        val fromEntityUUID: UUID,
        val fromEntityName: String,
        val amount: BigDecimal,
        val signatureOfApproval: String //base 64 encode ECDSA signature..
){
    fun toTrustLine(): TrustlineTransfer {
        return TrustlineTransfer(transferId = this.transferId,
                toEntityUUID = this.toEntityUUID,
                fromEntityUUID = this.fromEntityUUID,
                toEntityName = this.toEntityName,
                fromEntityName = this.fromEntityName,
                amount = this.amount

        )
    }
}

data class TrustLineUser(val userName: String,
                         val userId: UUID,
                         val publicKeyHandle: String,
                         val privateKeyHandle: String,
                         val role: UserRoles = UserRoles.USER)

enum class UserRoles {
    ADMIN,
    USER
}
