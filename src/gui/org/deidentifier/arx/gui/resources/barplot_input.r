colsin <- ncol(input)
rowsin <- nrow(input)
varnamesin <- names(input)

old.par <- par(mfrow=c(2,ceiling(colsin/2)), oma=c(4,0,2,0))

for (i in 1:colsin) {
	tabledata <- table(input[i])
	if (length(tabledata) < rowsin) {
		barplot(tabledata, ylab=varnamesin[i], las=2, main=varnamesin[i])	
	}
}

mtext("Barplots for all columns of Input", outer = TRUE, side = 3, cex=1.2)

par(old.par)