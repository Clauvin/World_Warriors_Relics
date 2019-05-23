package ww_relics.relics.character_cameos.sakura;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.AbstractCard.CardColor;
import com.megacrit.cardcrawl.cards.AbstractCard.CardRarity;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.rewards.RewardItem;

import basemod.abstracts.CustomRelic;
import ww_relics.resources.relic_graphics.GraphicResources;

public class SchoolBackpack extends CustomRelic {

	public static final String ID = "WW_Relics:School_Backpack";
	
	public static final int NUMBER_OF_EXTRA_CARDS = 5;
	public static final float CHANCE_OF_UPGRADED_CARDS = 0.1f;
	
	public static int number_of_cards_left = NUMBER_OF_EXTRA_CARDS;
	public static boolean battle_ended_before = false;
	public static RewardItem card_reward;
	public static ArrayList<String> card_reward_rarity = new ArrayList<String>();
	public static ArrayList<String> card_reward_id = new ArrayList<String>();
	public static ArrayList<Boolean> card_reward_upgrade = new ArrayList<Boolean>();
	public static String current_description;

	
	public static final Logger logger = LogManager.getLogger(SchoolBackpack.class.getName());
	
	public SchoolBackpack() {
		super(ID, GraphicResources.LoadRelicImage("White_Boots - steeltoe-boots - Lorc - CC BY 3.0.png"), 
				RelicTier.UNCOMMON, LandingSound.HEAVY);
		
		counter = NUMBER_OF_EXTRA_CARDS;
		
	}
	
	public String getUpdatedDescription() {
		if (current_description == null) return DESCRIPTIONS[0] + NUMBER_OF_EXTRA_CARDS + DESCRIPTIONS[1];
		else return current_description;
	}
	
	@Override
	public void atBattleStart() {
		
		if (counter <= 0) {
			current_description = DESCRIPTIONS[3];
			this.tips.clear();
			this.tips.add(new PowerTip(this.name, current_description));
			initializeTips();
		}
	}
	
	@Override
	public void atPreBattle() {
		
		if ((counter != number_of_cards_left)){
			number_of_cards_left = NUMBER_OF_EXTRA_CARDS;
			counter = NUMBER_OF_EXTRA_CARDS;
		}
		
		if ((number_of_cards_left > 0) && (AbstractDungeon.getCurrRoom().rewardAllowed)) {
			
			if (!battle_ended_before) {
				AddReward();
				number_of_cards_left--;
				counter = number_of_cards_left;
				if (counter <= 0) {
					number_of_cards_left = -2;
					counter = -2;
				}
			} else {
				AddSavedReward();
			}
		} else {
			if (counter <= 0) {
				number_of_cards_left = -2;
				counter = -2;
			}
		}
	}

	private void AddReward() {
		
		PlayerClass reward_class = getRandomBaseGameNotYoursPlayerClass();
		
		card_reward = new RewardItem();
		card_reward.cards.clear();
		card_reward.cards = createCardsFromOtherClassForReward(reward_class);
		card_reward.text = DESCRIPTIONS[1];
		AbstractDungeon.getCurrRoom().addCardReward(card_reward);
		flash();
		
	}
	
	private PlayerClass getRandomBaseGameNotYoursPlayerClass() {
		
		ArrayList<PlayerClass> basegame_classes = new ArrayList<PlayerClass>();
		
		for (PlayerClass base_game_player_class : PlayerClass.values()) {
			
			if (base_game_player_class != AbstractDungeon.player.chosenClass) {
				basegame_classes.add(base_game_player_class);
			}
			
		}
		
		return basegame_classes.get(AbstractDungeon.cardRng.random(basegame_classes.size() - 1));

	}
	
	private ArrayList<AbstractCard> createCardsFromOtherClassForReward(PlayerClass a_class) {
		
		ArrayList<AbstractCard> retVal = new ArrayList<AbstractCard>();
	    
	    int num_cards = 3;
	    num_cards = circunstancesThatChangeCardNumber(num_cards);
	    
	    AbstractCard.CardRarity rarity;
	    for (int i = 0; i < num_cards; i++)
	    {
	    	rarity = AbstractDungeon.rollRarity();
	    	AbstractCard card = null;
	    	switch (rarity)
	    	{
	    		case RARE: 
	    			AbstractDungeon.cardBlizzRandomizer = AbstractDungeon.cardBlizzStartOffset;
	    		break;
	    		case UNCOMMON: 
	    			break;
	    		case COMMON: 
	    			AbstractDungeon.cardBlizzRandomizer -= AbstractDungeon.cardBlizzGrowth;
			        if (AbstractDungeon.cardBlizzRandomizer <= AbstractDungeon.cardBlizzMaxOffset) {
			        	AbstractDungeon.cardBlizzRandomizer = AbstractDungeon.cardBlizzMaxOffset;
			        }
			        break;
	    		default: 
	    			logger.info("Paraphrasing the base game code: WTF?");
	    	}
	    	card = getCardAvoidingDuplicates(retVal, rarity, a_class);
	        if (card != null) {
	        	retVal.add(card);
	        }
	    }
	    ArrayList<AbstractCard> retVal2 = new ArrayList<AbstractCard>();
	    for (AbstractCard c : retVal) {
	    	retVal2.add(c.makeCopy());
	    }
	    for (AbstractCard c : retVal2) {
	    	if ((c.rarity != AbstractCard.CardRarity.RARE) && 
	    			(AbstractDungeon.cardRng.randomBoolean(CHANCE_OF_UPGRADED_CARDS)) && (c.canUpgrade())) {
	    		c.upgrade();
	    	} else if ((c.type == AbstractCard.CardType.ATTACK) && 
	    			(AbstractDungeon.player.hasRelic("Molten Egg 2"))) {
	    		c.upgrade();
	    	} else if ((c.type == AbstractCard.CardType.SKILL) &&
	    			(AbstractDungeon.player.hasRelic("Toxic Egg 2"))) {
	    		c.upgrade();
	    	} else if ((c.type == AbstractCard.CardType.POWER) &&
	    			(AbstractDungeon.player.hasRelic("Frozen Egg 2"))) {
		        c.upgrade();
	    	}
	    }
	    return retVal2;
	}
	
	private int circunstancesThatChangeCardNumber(int num_cards) {
		if (AbstractDungeon.player.hasRelic("Question Card")) {
			num_cards++;
		}
		if (AbstractDungeon.player.hasRelic("Busted Crown")) {
		    num_cards -= 2;
	    }
		if (ModHelper.isModEnabled("Binary")) {
		    num_cards--;
		}
		return num_cards;
	}

	private static AbstractCard getCardAvoidingDuplicates(ArrayList<AbstractCard> retVal, 
			CardRarity rarity, PlayerClass a_class) {
		
		AbstractCard card = null;
		
		boolean containsDupe = true;
		while (containsDupe)
		{
    		containsDupe = false;
    		card = getCard(rarity, a_class);

    		for (AbstractCard c : retVal) {
	        	if (c.cardID.equals(card.cardID))
	        	{
	        		containsDupe = true;
	        		break;
	        	}
	      	}
    	}
		
		return card;
	}	
	
	@SuppressWarnings("incomplete-switch")
	public static AbstractCard getCard(AbstractCard.CardRarity rarity, PlayerClass a_class)
	{
		CardGroup rare_class_CardPool = new CardGroup(CardGroupType.UNSPECIFIED);
		CardGroup uncommon_class_CardPool = new CardGroup(CardGroupType.UNSPECIFIED);
		CardGroup common_class_CardPool = new CardGroup(CardGroupType.UNSPECIFIED);
		
		CardColor class_color = CardColor.COLORLESS;
		
		if (a_class == PlayerClass.DEFECT) {
			class_color = CardColor.BLUE;
		}
		else if (a_class == PlayerClass.THE_SILENT) {
			class_color = CardColor.GREEN;
		}
		else if (a_class == PlayerClass.IRONCLAD) {
			class_color = CardColor.RED;
		}
		
		for (Map.Entry<String, AbstractCard> a_card : CardLibrary.cards.entrySet()) {
			AbstractCard one_more_card = a_card.getValue();
			if ((((AbstractCard)one_more_card).color == class_color) &&
				(((AbstractCard)one_more_card).type != AbstractCard.CardType.STATUS) &&
				(((AbstractCard)one_more_card).type != AbstractCard.CardType.CURSE)) {
				
				CardRarity this_card_rarity = ((AbstractCard)one_more_card).rarity;
			
				switch (this_card_rarity) {
					case RARE:
						rare_class_CardPool.addToBottom((AbstractCard)one_more_card);
						break;
				    case UNCOMMON: 
				    	uncommon_class_CardPool.addToBottom((AbstractCard)one_more_card);
				    	break;
				    case COMMON:
				    	common_class_CardPool.addToBottom((AbstractCard)one_more_card);
				    	break;
				}
					
			}
		}		
		
		switch (rarity)
		{
			case SPECIAL:  return rare_class_CardPool.getRandomCard(true);
		    case RARE:     return rare_class_CardPool.getRandomCard(true);
		    case UNCOMMON: return uncommon_class_CardPool.getRandomCard(true);
		    case COMMON:   return common_class_CardPool.getRandomCard(true);
		    case CURSE:    return common_class_CardPool.getRandomCard(true);
		    case BASIC:    return common_class_CardPool.getRandomCard(true);
	    }
	    logger.info("Paraphrasing the base code comment: No rarity on getCard in Abstract Dungeon");
	    return null;
	}
	
	public void AddSavedReward() {
		
		card_reward = new RewardItem();
		card_reward.cards = new ArrayList<AbstractCard>();
				
		int size = card_reward_id.size();
		
		for (int i = 0; i < size; i++) {
			
			AbstractCard reward_card;
						
			reward_card = CardLibrary.cards.get(card_reward_id.get(i)).makeCopy();
			
			logger.info(reward_card.name);
			
			if (card_reward_upgrade.get(i)) {
				reward_card.upgrade();
			}
			
			card_reward.text = DESCRIPTIONS[1];
			card_reward.cards.add(reward_card);	
		
		}
			
		AbstractDungeon.getCurrRoom().addCardReward(card_reward);
		flash();
		
	}

	
	public static void save(final SpireConfig config) {

        if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(ID)) {
    		logger.info("Started saving School Backpack information");

    		if (AbstractDungeon.isDungeonBeaten || AbstractDungeon.player.isDead) {
    			clear(config);
    		} 
    		else {
    			
    			if (AbstractDungeon.getCurrRoom().isBattleOver) {
        			config.setInt("school_backpack_1", number_of_cards_left + 1);
        		}
        		else config.setInt("school_backpack_1", number_of_cards_left);
                
                if (AbstractDungeon.getCurrRoom().isBattleOver) {
                	config.setBool("school_backpack_3", true);
                } else {
                	config.setBool("school_backpack_3", false);
                }
                
                
                
                if (card_reward == null) {
                	config.setInt("school_backpack_reward_size", 0);
                } else config.setInt("school_backpack_reward_size", card_reward.cards.size());
                
                if (card_reward != null) {
                	for (int i = 0; i < card_reward.cards.size(); i++) {
                    	config.setString("school_backpack_reward_" + String.valueOf(i),
                    			card_reward.cards.get(i).cardID);
                    	config.setString("school_backpack_reward_rarity_" + String.valueOf(i),
                    			card_reward.cards.get(i).rarity.toString());
                    	config.setBool("school_backpack_reward_upgrade_" + String.valueOf(i),
                    			card_reward.cards.get(i).upgraded);
                    }
                }
    			
                try {
    				config.save();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}

            logger.info("Finished saving School Backpack information");
        }
        else {
        	clear(config);
        }

    }
	
	public static void load(final SpireConfig config) {
		
		logger.info("Loading School Backpack info.");
		if (AbstractDungeon.player.hasRelic(ID) && config.has("school_backpack_1")) {

			number_of_cards_left = config.getInt("school_backpack_1");
			
			int size = config.getInt("school_backpack_reward_size");
			
			for (int i = 0; i < size; i++) {
				
				card_reward_rarity.add(config.getString("school_backpack_reward_rarity_" + String.valueOf(i)));			
				card_reward_id.add(config.getString("school_backpack_reward_" + String.valueOf(i)));
				card_reward_upgrade.add(config.getBool("school_backpack_reward_upgrade_" + String.valueOf(i)));
			
			}
			
			battle_ended_before = config.getBool("school_backpack_3");
			
            try {
				config.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            logger.info("Finished loading School Backpack info.");
        }
		
		else
		{
			logger.info("There's no info, setting variables accordingly.");

			logger.info("Finished setting School Backpack variables.");
		}
		
		
    }
	
	public static void clear(final SpireConfig config) {
		logger.info("Clearing School Backpack variables.");
        config.remove("school_backpack_1");
        config.remove("school_backpack_2");
        
        if (config.has("school_backpack_reward_size")) {
        	int size = config.getInt("school_backpack_reward_size");
        
			for (int i = 0; i < size; i++) {
				
				config.remove("school_backpack_reward_rarity_" + String.valueOf(i));			
				config.remove("school_backpack_reward_" + String.valueOf(i));
				config.remove("school_backpack_reward_upgrade_" + String.valueOf(i));
			
			}

        }
        
        logger.info("Finished clearing School Backpack variables.");
	}
	
	@Override
	public CustomRelic makeCopy() {
		return new SchoolBackpack();
	}

}