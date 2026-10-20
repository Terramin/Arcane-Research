package arcaneresearch.common.lib.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

// NEOFORGE_PORT: new shared base (no Forge counterpart file; Forge used
// net.minecraftforge.fluids.capability.templates.FluidTank directly in each tile).
// FluidTank was REMOVED in the 21.9 transfer rework; this base re-implements the exact
// FluidTank surface our tiles used (int fill/drain with EXECUTE semantics, getFluidAmount,
// getFluid, writeToNBT/readFromNBT with the same "FluidName"/"Amount" keys) on top of
// FluidStacksResourceHandler (transactional ResourceHandler<FluidResource>).
// Per-tile validation (water-only etc.) is injected via Predicate, mirroring the old
// anonymous `isFluidValid` overrides.
public class ArcaneresearchFluidTank extends FluidStacksResourceHandler {

    private final int capacity;
    private final Predicate<FluidStack> validator;

    public ArcaneresearchFluidTank(int capacity) {
        this(capacity, stack -> true);
    }

    public ArcaneresearchFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(net.minecraft.core.NonNullList.withSize(1, FluidStack.EMPTY), capacity);
        this.capacity = capacity;
        this.validator = validator;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource == null || resource.isEmpty()) return false;
        return validator.test(resource.toStack(1));
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }

    public int getFluidAmount() {
        return getAmountAsInt(0);
    }

    // NEOFORGE_PORT: restores the Forge FluidTank getCapacity() surface
    // (base keeps protected getCapacity(int, FluidResource)).
    public int getCapacity() {
        return capacity;
    }

    public FluidStack getFluid() {
        return FluidUtil.getStack(this, 0);
    }

    public int fill(FluidStack stack, boolean execute) {
        if (stack == null || stack.isEmpty()) return 0;
        FluidResource resource = FluidResource.of(stack);
        if (!isValid(0, resource)) return 0;
        try (var tx = Transaction.openRoot()) {
            int filled = insert(0, resource, stack.getAmount(), tx);
            if (execute) tx.commit();
            return filled;
        }
    }

    public FluidStack drain(int maxDrain, boolean execute) {
        if (maxDrain <= 0) return FluidStack.EMPTY;
        FluidResource resource = getResource(0);
        if (resource == null || resource.isEmpty()) return FluidStack.EMPTY;
        try (var tx = Transaction.openRoot()) {
            int drained = extract(0, resource, maxDrain, tx);
            if (execute) tx.commit();
            if (drained <= 0) return FluidStack.EMPTY;
            Fluid fluid = resource.value();
            return fluid == null || fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, drained);
        }
    }

    public FluidStack drain(FluidStack resource, boolean execute) {
        if (resource == null || resource.isEmpty()) return FluidStack.EMPTY;
        FluidResource current = getResource(0);
        if (current == null || current.isEmpty()) return FluidStack.EMPTY;
        if (current.value() != resource.getFluid()) return FluidStack.EMPTY;
        return drain(resource.getAmount(), execute);
    }

    public void writeToNBT(CompoundTag tag) {
        FluidStack stack = getFluid();
        if (stack == null || stack.isEmpty()) {
            tag.putString("FluidName", "minecraft:empty");
            tag.putInt("Amount", 0);
            return;
        }
        Identifier id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        tag.putString("FluidName", id != null ? id.toString() : "minecraft:empty");
        tag.putInt("Amount", stack.getAmount());
    }

    public void readFromNBT(CompoundTag tag) {
        Fluid fluid = Fluids.EMPTY;
        int amount = 0;
        if (tag.contains("FluidName")) {
            String name = tag.getStringOr("FluidName", "");
            if (!name.isEmpty() && !name.equals("minecraft:empty")) {
                Fluid parsed = BuiltInRegistries.FLUID.getValue(Identifier.parse(name));
                if (parsed != null && parsed != Fluids.EMPTY) {
                    fluid = parsed;
                    amount = tag.getIntOr("Amount", 0);
                }
            }
        }
        clearAndFill(fluid, amount);
    }

    // NEOFORGE_PORT: ValueOutput/ValueInput overloads for tile persistence (26.x).
    // Same "FluidName"/"Amount" keys and semantics as the CompoundTag versions.
    public void writeToNBT(net.minecraft.world.level.storage.ValueOutput out) {
        FluidStack stack = getFluid();
        if (stack == null || stack.isEmpty()) {
            out.putString("FluidName", "minecraft:empty");
            out.putInt("Amount", 0);
            return;
        }
        Identifier id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        out.putString("FluidName", id != null ? id.toString() : "minecraft:empty");
        out.putInt("Amount", stack.getAmount());
    }

    public void readFromNBT(net.minecraft.world.level.storage.ValueInput in) {
        Fluid fluid = Fluids.EMPTY;
        int amount = 0;
        String name = in.getStringOr("FluidName", "");
        if (!name.isEmpty() && !name.equals("minecraft:empty")) {
            Fluid parsed = BuiltInRegistries.FLUID.getValue(Identifier.parse(name));
            if (parsed != null && parsed != Fluids.EMPTY) {
                fluid = parsed;
                amount = in.getIntOr("Amount", 0);
            }
        }
        clearAndFill(fluid, amount);
    }

    // Loading overwrites the slot directly: extract() rejects an empty resource (NeoForge
    // transfer preconditions), which crashed every empty-tank sync packet on the client.
    private void clearAndFill(Fluid fluid, int amount) {
        if (amount > 0 && fluid != Fluids.EMPTY) {
            set(0, FluidResource.of(fluid), Math.min(amount, capacity));
        } else {
            set(0, FluidResource.EMPTY, 0);
        }
    }
}
