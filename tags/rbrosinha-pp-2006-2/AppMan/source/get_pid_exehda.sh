ps -fu dalto | grep exehda | gawk 'BEGIN{tmp=" "}{tmp= tmp" "$2}END{print tmp}' 
