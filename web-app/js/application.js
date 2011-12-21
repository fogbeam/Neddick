
            function afterVote(e) {
             
               var entryId = e.responseJSON.resp.entryId;
               var score = e.responseJSON.resp.score;
       
               var scoreDiv = document.getElementById("score."+entryId);
               scoreDiv.innerHTML = score;
                    
            }
                    
            function afterSave(e) {
            
               alert( 'saved entry' );
            }
            
            function afterHide(e) {
            
               alert( 'hid entry' );
            }

               
            function openShareDialog(entryId) {
               window.open( "/neddick1/share/index/?entryId=" + entryId, "Neddick - Share", 
                    "status = 1, height = 300, width = 300, resizable = 0"  )
            }