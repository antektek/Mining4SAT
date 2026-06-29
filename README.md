# Mining4SAT

## Options available

The following options are available to restrict Mining4SAT to only one form of compression:

* -n or -N: launch only non binary reduction

* -b or -B: launch only binary reduction

If no option is specified, both non binary and binary reductions are launched.

## Command line

Mining4Sat can be launched with the following command line:

```
java -jar mining4Sat.jar file.cnf lambda [option]
```

## Benchmarks

The repository *benchmarks* contains several families of benchmarks used in our experiments.

The repository *SAT24* contains a single file (*track_main_2024.uri*). It can be used to download all the main track instances of the SAT 2024 competition with the following command line:

```
wget --content-disposition -i track_main_2024.uri
```

The repository *Crypto* contains XNF instances. We also provide scripts to compress the CNF or XOR part of an instance or of all the instances contained in a directory.
