package arcaneresearch.api.crafting;

import arcaneresearch.common.lib.aura.NodeType;

public class Part {

    private Object source;

    private Object target;

    private boolean opp;

    private int priority;

    private boolean applyPlayerFacing;

    /**
     * Тип аура-ноды для отображения в книге/JEI: сам блок ноды невидим
     * (RenderShape.INVISIBLE), поэтому BlueprintBlockAccess показывает
     * суррогат, а рендерер книги рисует настоящую ноду этого типа.
     * null — обычная нода.
     */
    private NodeType nodeType;

    public Part(Object source, Object target, boolean opp, int priority) {
        this.source = source;
        this.target = target;
        this.opp = opp;
        this.priority = priority;
    }

    public Part(Object source, Object target, boolean opp) {
        this(source, target, opp, 50);
    }

    public Part(Object source, Object target) {
        this(source, target, false, 50);
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean isOpp() {
        return opp;
    }

    public void setOpp(boolean opp) {
        this.opp = opp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean getApplyPlayerFacing() {
        return applyPlayerFacing;
    }

    public Part setApplyPlayerFacing(boolean applyFacing) {
        this.applyPlayerFacing = applyFacing;
        return this;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Part setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }
}
