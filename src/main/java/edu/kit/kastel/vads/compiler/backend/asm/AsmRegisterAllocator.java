package edu.kit.kastel.vads.compiler.backend.asm;

import edu.kit.kastel.vads.compiler.backend.aasm.VirtualRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AsmRegisterAllocator implements RegisterAllocator {

    private static final Register[] REGISTERS = {
            new AsmRegister("eax"),
            new AsmRegister("ebx"),
            new AsmRegister("ecx"),
            new AsmRegister("edx"),
            new AsmRegister("esi"),
            new AsmRegister("edi"),
            new AsmRegister("r8d"),
            new AsmRegister("r9d"),
            new AsmRegister("r10d"),
            new AsmRegister("r11d"),
            new AsmRegister("r12d"),
            new AsmRegister("r13d"),
            new AsmRegister("r14d"),
            new AsmRegister("r15d")
    };


    private int id;
    private final Map<Node, Register> registers = new HashMap<>();

    @Override
    public Map<Node, Register> allocateRegisters(IrGraph graph) {
        Set<Node> visited = new HashSet<>();
        visited.add(graph.endBlock());
        scan(graph.endBlock(), visited);
        return Map.copyOf(this.registers);
    }

    private void scan(Node node, Set<Node> visited) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited);
            }
        }
        if (needsRegister(node)) {
            this.registers.put(node, REGISTERS[id++%REGISTERS.length]);
        }
    }

    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block || node instanceof ReturnNode);
    }
}
