boxplot_numerics <- function(titel, s) {
	nums <- sapply(titel, is.numeric)
	colsin <- table(nums)["TRUE"]
	
	rowsin <- nrow(titel)
	varnamesin <- names(titel)
	
	if (is.na(colsin)){
		cat(paste(s," does not contain numeric columns.\n"))
	} else if (colsin == 1) {
		old.par <- par(mfrow=c(1,1), oma=c(0,0,2,0))
	} else {
		old.par <- par(mfrow=c(2,ceiling(colsin/2)), oma=c(0,0,2,0))
	}
	
	if (!is.na(colsin)){
		
		if (rowsin >= 1) {
			for (i in 1:ncol(titel)) {
				if (is.numeric(titel[1,i])) {
					boxplot(titel[i], ylab=varnamesin[i], main=varnamesin[i])		
				}
			}
		} 
		
		mtext(paste("Boxplots for numeric values of ", s), outer = TRUE, side = 3, cex=1.2)
		
		par(old.par)
	} 
}