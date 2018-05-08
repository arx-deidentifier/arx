colsin <- ncol(input)
colsout <- ncol(output)

varnamesin <- names(input)
varnamesout <- names(output)

horizontaldimension <- ceiling(max(colsin,colsout)/2)

old.par <- par(mfrow=c(4,horizontaldimension), oma=c(0,2,2,0))  #colsin should be equal to colsout. Otherwise nothing crashes, 
												#but it might look not as intended.

for (i in 1:horizontaldimension) {
	barplot(table(input[i]), ylab=varnamesin[i], las=2, main=varnamesin[i])
}
for (i in 1:horizontaldimension) {
	barplot(table(output[i]), ylab=varnamesout[i], las=2, main=varnamesout[i])
}

for (i in (horizontaldimension+1):colsin) {
	barplot(table(input[i]), ylab=varnamesin[i], las=2, main=varnamesin[i])
}
if (ceiling(max(colsin,colsout)/2) > max(colsin,colsout)/2) {
	plot(0,type='n',axes=FALSE,ann=FALSE)
}
for (i in (horizontaldimension+1):colsout) {
	barplot(table(output[i]), ylab=varnamesout[i], las=2, main=varnamesout[i])
}

mtext("Barplots for comparison of Input and Output", outer = TRUE, side = 3, cex=1.2)
mtext("INPUT", outer = TRUE, side = 2, adj=0.9)
mtext("OUTPUT", outer = TRUE, side = 2, adj=0.65)
mtext("INPUT", outer = TRUE, side = 2, adj=0.4)
mtext("OUTPUT", outer = TRUE, side = 2, adj=0.1)

par(old.par)