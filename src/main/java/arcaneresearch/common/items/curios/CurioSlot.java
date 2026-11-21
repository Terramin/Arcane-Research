package arcaneresearch.common.items.curios;

public enum CurioSlot {
    AMULET("necklace"),
    RING("ring"),
    BELT("belt"),
    CHARM("charm"),
    HEAD("head");

    public final String slotId;

    CurioSlot(String slotId) {
        this.slotId = slotId;
    }
}
