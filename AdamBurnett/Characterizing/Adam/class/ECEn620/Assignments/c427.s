	.file	"c427.c"
.globl input
	.data
	.align 4
	.type	input, @object
	.size	input, 4
input:
	.long	10
.globl my_init_array
	.align 16
	.type	my_init_array, @object
	.size	my_init_array, 24
my_init_array:
	.long	1
	.long	2
	.long	3
	.long	4
	.long	5
	.long	6
	.text
.globl factorial
	.type	factorial, @function
factorial:
.LFB2:
	pushq	%rbp
.LCFI0:
	movq	%rsp, %rbp
.LCFI1:
	subq	$8, %rsp
.LCFI2:
	movl	%edi, -4(%rbp)
	cmpl	$1, -4(%rbp)
	jne	.L2
	movl	$1, -8(%rbp)
	jmp	.L4
.L2:
	movl	-4(%rbp), %edi
	subl	$1, %edi
	call	factorial
	movl	%eax, %edx
	imull	-4(%rbp), %edx
	movl	%edx, -8(%rbp)
.L4:
	movl	-8(%rbp), %eax
	leave
	ret
.LFE2:
	.size	factorial, .-factorial
	.section	.rodata
.LC0:
	.string	"abcdefg"
	.text
.globl main
	.type	main, @function
main:
.LFB3:
	pushq	%rbp
.LCFI3:
	movq	%rsp, %rbp
.LCFI4:
	movl	$.LC0, %edi
	call	puts
	movl	input(%rip), %edi
	call	factorial
	movl	$0, %eax
	leave
	ret
.LFE3:
	.size	main, .-main
	.comm	my_array,8192,32
	.section	.eh_frame,"a",@progbits
.Lframe1:
	.long	.LECIE1-.LSCIE1
.LSCIE1:
	.long	0x0
	.byte	0x1
	.string	"zR"
	.uleb128 0x1
	.sleb128 -8
	.byte	0x10
	.uleb128 0x1
	.byte	0x3
	.byte	0xc
	.uleb128 0x7
	.uleb128 0x8
	.byte	0x90
	.uleb128 0x1
	.align 8
.LECIE1:
.LSFDE1:
	.long	.LEFDE1-.LASFDE1
.LASFDE1:
	.long	.LASFDE1-.Lframe1
	.long	.LFB2
	.long	.LFE2-.LFB2
	.uleb128 0x0
	.byte	0x4
	.long	.LCFI0-.LFB2
	.byte	0xe
	.uleb128 0x10
	.byte	0x86
	.uleb128 0x2
	.byte	0x4
	.long	.LCFI1-.LCFI0
	.byte	0xd
	.uleb128 0x6
	.align 8
.LEFDE1:
.LSFDE3:
	.long	.LEFDE3-.LASFDE3
.LASFDE3:
	.long	.LASFDE3-.Lframe1
	.long	.LFB3
	.long	.LFE3-.LFB3
	.uleb128 0x0
	.byte	0x4
	.long	.LCFI3-.LFB3
	.byte	0xe
	.uleb128 0x10
	.byte	0x86
	.uleb128 0x2
	.byte	0x4
	.long	.LCFI4-.LCFI3
	.byte	0xd
	.uleb128 0x6
	.align 8
.LEFDE3:
	.ident	"GCC: (GNU) 4.1.2 20080704 (Red Hat 4.1.2-54)"
	.section	.note.GNU-stack,"",@progbits
