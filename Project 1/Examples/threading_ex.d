import std.stdio;
import std.parallelism : taskPool;

void main() {
	foreach (i, val; taskPool.parallel(new int[50])) {
	    writeln("Hello:", i);
}}
