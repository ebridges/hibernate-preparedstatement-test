#!perl


for($i=1; $i<=10000; $i++) {
    my $simple_value=$i<<4;
    my $big_text = 'a'.'a'x($i%1000);
    my $amount = 0;
    if($i>1) {
	$amount = 1/($i);
	$amount = substr($amount, 0, 12)
	    if(length $amount > 12);
    }
     
    print qq|INSERT INTO scratch..hibernate_test_table (id, name, last_modified, simple_value, big_text, amount) VALUES ($i, 'xxxxxxxx$i', getdate(), $simple_value, '$big_text', $amount)\ngo\n|;
}
