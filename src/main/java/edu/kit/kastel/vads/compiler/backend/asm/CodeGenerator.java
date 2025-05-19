package edu.kit.kastel.vads.compiler.backend.asm;


import edu.kit.kastel.vads.compiler.backend.aasm.AasmRegisterAllocator;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {

    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                
                .global main
                """);
        for (IrGraph graph : program) {
            builder.append(String.format(".global _%s\n", graph.name()));
        }
        //Data section for variables
        builder.append("""
                .data
                """);
        for (IrGraph graph : program) {
            builder.append(String.format("_%s:\n", graph.name()));

            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);
            for (Register register : registers.values()) {
                builder.append(register.toString()+":\n").append(".long 0\n");
            }
        }

        builder.append("""
                .text
                        main:
                        call _main
                                # move the return value into the first argument for the syscall
                        movq %rax, %rdi
                        # move the exit syscall number into rax
                        movq $0x3C, %rax
                                syscall
                """);

        for (IrGraph graph : program) {
            builder.append(String.format("_%s:\n", graph.name()));

            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);
            generateForGraph(graph, builder, registers);
        }
        return builder.toString();
    }


    private void generateForGraph(IrGraph graph, StringBuilder builder, Map<Node, Register> registers) {
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, builder, registers);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers);
            }
        }

        switch (node) {
            case AddNode add -> binary(builder, registers, add, "addl");
            case SubNode sub -> binary(builder, registers, sub, "subl");
            case MulNode mul -> binary(builder, registers, mul, "imull");
            case DivNode div -> {
                builder.repeat(" ", 2)
                        .append("movl ").append(registers.get(predecessorSkipProj(div, BinaryOperationNode.LEFT))).append(",%eax\n")
                        .append("  cdq\n")
                        .append("  idivl ").append(registers.get(predecessorSkipProj(div, BinaryOperationNode.RIGHT))).append("\n")
                        .append("  movl %eax,").append(registers.get(div));
            }
            case ModNode mod -> binary(builder, registers, mod, "imodl");
            case ReturnNode r ->
                    builder.repeat(" ", 2).append("movl ").append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT))).append("(,1)").append(",%eax\n").append("  ret");
            case ConstIntNode c ->
                    builder.repeat(" ", 2).append("movl $").append(c.value()).append(",").append(registers.get(c));
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private static void binary(StringBuilder builder, Map<Node, Register> registers, BinaryOperationNode node, String opcode) {
        builder.repeat(" ", 2)
                .append(String.format("movl %s(,1),%%ebx\n movl %s(,1),%%ecx\n %s %%ebx,%%ecx\nmovl %%ecx,%s\n",
                        registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)),
                        registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)),
                        opcode,
                        registers.get(node)));
    }
}
