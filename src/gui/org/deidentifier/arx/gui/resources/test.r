n <- floor(runif(1000)*10)
t <- table(n)
print(summary(t))
barplot(t,xlab="Generated Numbers", ylab="Occurences", border="red")
