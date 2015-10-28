public class Piece{
	private boolean checkteam;
	private Board boardlocation;
	private int initx;
	private int inity;
	private String piecetype;
	private boolean has_captured = false;
	private int differenceX;
    private int differenceY;
    private int capturedX;
    private int capturedY;
    private boolean isking = false;
	
	public Piece(boolean isFire, Board b, int x, int y, String type){
		checkteam = isFire;
		boardlocation = b;
		initx = x;
		inity = y;
		piecetype = type;
	}

	public int side(){
		if(checkteam)
			return 0;
		return 1;
	}

	public boolean isFire(){
         return checkteam;
	}
	
	public boolean isKing(){
        return isking;
	}

	public boolean isBomb(){
		if(piecetype != "bomb")
			return false;
		return true;
	}

	public boolean isShield(){
		if(piecetype != "shield")
			return false;
		return true;
	}


	public boolean hasCaptured(){
		if(has_captured){
			return true;
		}else{
			return false;
		}
	}

	public void doneCapturing(){
		if(has_captured)
			has_captured = false;
	}

	public void move(int x, int y){
		boardlocation.place(this,x,y);
		if (checkteam){
			if(y==7){
				isking = true;
			}
		}else{
			if(y==0){
				isking = true;
			}
		}
		boardlocation.remove(initx,inity);
		differenceX=x-initx;
		differenceY=y-inity;
		if(Math.abs(differenceX)==2 && Math.abs(differenceY)==2){	
			capturedX = initx + differenceX/2;
			capturedY = inity + differenceY/2;
			if(isBomb()){			
				boardlocation.remove(capturedX,capturedY);
				for(int d=x-1;d<x+2;d++){
		            for(int e=y-1;e<y+2;e++){
		                if((d<8)&&(e<8)&&(d>0)&&(e>0)&&(boardlocation.pieceAt(d,e)!= null)){
			                if(!boardlocation.pieceAt(d,e).isShield()){
			                    	boardlocation.remove(d,e);
			                    }
			                }
		                }
		            }
	        }else{
	        	boardlocation.remove(capturedX,capturedY);
	        }		        	
	        has_captured=true;
		    }
		initx = x;
		inity = y;
		}


	
}

        
                    	
                    	

     