package arcaneresearch.api.research;

import net.minecraft.world.entity.Entity;

/**
 * Результат сканирования цели таумометром.
 * Порт {@code thaumcraft.api.research.ScanResult} (TC 4.2.3.5):
 * {@code type} 1 — блок/предмет ({@code id/meta} не используются нашим
 * сканом — цель задаётся позицией), 2 — сущность, 3 — явление
 * ({@code phenomena}, в ядре только {@code "NODE"+id}).
 */
public class ScanResult {

    public byte type = 0;

    public int id;

    public int meta;

    public Entity entity;

    public String phenomena;

    public ScanResult(byte type, int blockId, int blockMeta, Entity entity, String phenomena) {
        this.type = type;
        this.id = blockId;
        this.meta = blockMeta;
        this.entity = entity;
        this.phenomena = phenomena;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScanResult sr) {
            if (this.type != sr.type) {
                return false;
            }
            if (this.type == 1 && (this.id != sr.id || this.meta != sr.meta)) {
                return false;
            }
            if (this.type == 2 && (this.entity == null || sr.entity == null
                    || this.entity.getId() != sr.entity.getId())) {
                return false;
            }
            if (this.type == 3 && (this.phenomena == null
                    || !this.phenomena.equals(sr.phenomena))) {
                return false;
            }
        }
        return true;
    }
}
