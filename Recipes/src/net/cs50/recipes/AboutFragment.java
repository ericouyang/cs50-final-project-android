package net.cs50.recipes;
 
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
 
public class AboutFragment extends Fragment {
    
	String aboutText = "nom! was created by Eric Ouyang, Fred Widjaja, and Michael Hwang for their CS50 Final Project. ";
	
    public AboutFragment(){}
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
  
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        
        TextView textListGroup = (TextView) container.findViewById(R.id.text_list_group);
        textListGroup.setText(aboutText);
        
        return rootView;
    }
}


