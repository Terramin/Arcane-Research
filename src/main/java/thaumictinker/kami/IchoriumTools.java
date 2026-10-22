package thaumictinker.kami;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

 



 
public final class IchoriumTools {

    private IchoriumTools() {
    }

    public static class Sword extends SwordItem {
        public Sword(Tier tier, Item.Properties properties) {
            super(tier, 4, -2.4F, properties);
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }
    }

    public static class Pickaxe extends PickaxeItem {
        public Pickaxe(Tier tier, Item.Properties properties) {
            super(tier, 2, -2.8F, properties);
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }
    }

    public static class Shovel extends ShovelItem {
        public Shovel(Tier tier, Item.Properties properties) {
            super(tier, 1, -3.0F, properties);
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }
    }

    public static class Axe extends AxeItem {
        public Axe(Tier tier, Item.Properties properties) {
            super(tier, 3.0F, -3.0F, properties);
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }
    }
}
