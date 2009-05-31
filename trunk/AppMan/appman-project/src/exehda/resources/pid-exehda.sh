ps -fu kayser | grep exehda | gawk 'BEGIN{tmp=" "}{tmp= tmp" "$2}END{print tmp}' 
