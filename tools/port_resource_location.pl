# PORT(26.3-fabric) helper. Usage: perl tools/port_resource_location.pl < In.java > Out.java
# ResourceLocation (1.20.1) -> Identifier (26.3). Reads a Java file on stdin, writes the result to stdout.
# Constructor arguments are split at top level with paren/bracket/brace and string-literal awareness.
use strict;
use warnings;

local $/;
my $s = <STDIN>;
my $BS = chr(92);    # backslash, spelled out so no shell layer can eat it
my $DQ = chr(34);
my $SQ = chr(39);

sub find_close {
    my ($str, $start) = @_;
    my ($depth, $i, $len) = (0, $start, length $str);
    while ($i < $len) {
        my $c = substr($str, $i, 1);
        if ($c eq $DQ || $c eq $SQ) {
            my $q = $c;
            $i++;
            while ($i < $len && substr($str, $i, 1) ne $q) {
                $i += (substr($str, $i, 1) eq $BS) ? 2 : 1;
            }
        } elsif ($c eq '(' || $c eq '[' || $c eq '{') {
            $depth++;
        } elsif ($c eq ')' || $c eq ']' || $c eq '}') {
            $depth--;
            return $i + 1 if $depth == 0;
        }
        $i++;
    }
    return -1;
}

sub count_top_level_args {
    my ($inner) = @_;
    my ($depth, $count, $len) = (0, 1, length $inner);
    for (my $i = 0; $i < $len; $i++) {
        my $c = substr($inner, $i, 1);
        if ($c eq $DQ || $c eq $SQ) {
            my $q = $c;
            $i++;
            while ($i < $len && substr($inner, $i, 1) ne $q) {
                $i++ if substr($inner, $i, 1) eq $BS;
                $i++;
            }
            next;
        }
        $depth++ if $c eq '(' || $c eq '[' || $c eq '{';
        $depth-- if $c eq ')' || $c eq ']' || $c eq '}';
        $count++ if $c eq ',' && $depth == 0;
    }
    return $count;
}

my $out = '';
my $pos = 0;
while ($s =~ /new\s+ResourceLocation\s*\(/g) {
    my $mstart = $-[0];
    my $open = $+[0] - 1;
    my $close = find_close($s, $open);
    die "unbalanced constructor at offset $mstart\n" if $close < 0;
    my $inner = substr($s, $open + 1, $close - $open - 2);
    my $arity = count_top_level_args($inner);
    my $repl;
    if ($arity == 2) {
        $repl = "Identifier.fromNamespaceAndPath($inner)";
    } elsif ($arity == 1) {
        $repl = "Identifier.parse($inner)";
    } else {
        die "unexpected arity $arity at offset $mstart\n";
    }
    $out .= substr($s, $pos, $mstart - $pos) . $repl;
    $pos = $close;
    pos($s) = $close;
}
$out .= substr($s, $pos);
$s = $out;

# isValidResourceLocation(x): decompiled 26.3 Identifier.tryParse returns null exactly when 1.20.1 returned false.
$out = '';
$pos = 0;
while ($s =~ /\bResourceLocation\.isValidResourceLocation\s*\(/g) {
    my $mstart = $-[0];
    my $open = $+[0] - 1;
    my $close = find_close($s, $open);
    die "unbalanced isValidResourceLocation at offset $mstart\n" if $close < 0;
    my $inner = substr($s, $open + 1, $close - $open - 2);
    $out .= substr($s, $pos, $mstart - $pos) . "(Identifier.tryParse($inner) != null)";
    $pos = $close;
    pos($s) = $close;
}
$out .= substr($s, $pos);
$s = $out;

$s =~ s/\bResourceLocation\.tryParse\(/Identifier.tryParse(/g;
$s =~ s/\bnet\.minecraft\.resources\.ResourceLocation\b/net.minecraft.resources.Identifier/g;
$s =~ s/\bResourceLocation\b/Identifier/g;
print $s;
