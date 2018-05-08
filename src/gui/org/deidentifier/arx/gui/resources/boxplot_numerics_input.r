nums <- sapply(input, is.numeric)
colsin <- table(nums)["TRUE"]

rowsin <- nrow(input)
varnamesin <- names(input)

if (colsin == 1) {
	old.par <- par(mfrow=c(1,1), oma=c(0,0,2,0))
} else {
	old.par <- par(mfrow=c(2,ceiling(colsin/2)), oma=c(0,0,2,0))
}

if (rowsin >= 1) {
	for (i in 1:ncol(input)) {
		if (is.numeric(input[1,i])) {
			boxplot(input[i], ylab=varnamesin[i], main=varnamesin[i])		
		}
	}
} 

mtext("Boxplots for numeric values of Input", outer = TRUE, side = 3, cex=1.2)

par(old.par)