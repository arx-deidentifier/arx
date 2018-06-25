colsin <- ncol(input)
colsout <- ncol(output)
rowsin <- nrow(input)
rowsout <- nrow(output)

cat("Number of columns of input:")
print(colsin)
cat("Number of columns of output:")
print(colsout)
cat("Number of rows of input:") 
print(rowsin)
cat("Number of rows of output:") 
print(rowsout)

cat("Summary input: \n")
for (i in 1:colsin) {
	print(summary(input[i]))
	cat("\n")
}

cat("Summary output: \n")
for (i in 1:colsout) {
	print(summary(output[i]))
	cat("\n")
}