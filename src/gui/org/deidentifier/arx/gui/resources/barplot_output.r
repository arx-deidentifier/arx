colsout <- ncol(output)
rowsout <- nrow(output)
varnamesout <- names(output)

old.par <- par(mfrow=c(2,ceiling(colsout/2)), oma=c(4,0,2,0))

for (i in 1:colsout) {
	tabledata <- table(output[i])
	if (length(tabledata) < rowsout) {
		barplot(tabledata, ylab=varnamesout[i], las=2, main=varnamesout[i])	
	}
}

mtext("Barplots for all columns of Output", outer = TRUE, side = 3, cex=1.2)

par(old.par)